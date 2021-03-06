package org.silverpeas.components.suggestionbox.model;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.components.suggestionbox.notification
    .SuggestionBoxSubscriptionUserNotification;
import org.silverpeas.components.suggestionbox.notification
    .SuggestionPendingValidationUserNotification;
import org.silverpeas.components.suggestionbox.notification.SuggestionValidationUserNotification;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.model.ContributionValidation;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.Process;

import javax.enterprise.inject.Vetoed;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY.*;
import static org.silverpeas.core.contribution.ContributionStatus.*;

/**
 * A collection of suggestions enriched with business methods on them. A collection of suggestions
 * belongs always to a given suggestion box.
 * @author mmoquillon
 */
@Vetoed
public class SuggestionCollection implements Collection<Suggestion> {

  private SuggestionBox suggestionBox;

  protected SuggestionCollection(final SuggestionBox suggestionBox) {
    this.suggestionBox = suggestionBox;
  }

  @Override
  public int size() {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    return actual.persistedSuggestions().size();
  }

  @Override
  public boolean isEmpty() {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    return actual.persistedSuggestions().isEmpty();
  }

  @Override
  public boolean contains(final Object suggestion) {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    return actual.persistedSuggestions().contains(suggestion);
  }

  @Override
  public Iterator<Suggestion> iterator() {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    return actual.persistedSuggestions().iterator();
  }

  @Override
  public Object[] toArray() {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    return actual.persistedSuggestions().toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    return actual.persistedSuggestions().toArray(a);
  }

  @Override
  public boolean add(final Suggestion suggestion) {
    add(suggestion, null);
    return true;
  }

  /**
   * Adds the specified suggestion among the other suggestions of the suggestion box.
   * <p/>
   * The suggestion will be persisted automatically once added.
   * @param suggestion the suggestion to add.
   * @param uploadedFiles a collection of file to attach to the suggestion.
   */
  public void add(final Suggestion suggestion, final Collection<UploadedFile> uploadedFiles) {
    Transaction.performInOne((Process<Void>) () -> {
      final SuggestionRepository suggestionRepository = getSuggestionRepository();
      SuggestionBox actual =
          SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
      suggestion.setSuggestionBox(actual);
      actual.persistedSuggestions().add(suggestion);
      suggestionRepository.save(suggestion);

      // Attach uploaded files
      Attachments.from(uploadedFiles).attachTo(suggestion);
      return null;
    });

  }

  /**
   * Removes the specified suggestion from the suggestion box.
   * <p/>
   * If the suggestion doesn't exist in the suggestion box, then nothing is done.
   * @param aSuggestion the suggestion to remove.
   * @return true if the suggestion exists in this collection and then is removed.
   */
  @Override
  public boolean remove(Object aSuggestion) {
    final Suggestion suggestion = (Suggestion) aSuggestion;
    return Transaction.performInOne(() -> {
      final SuggestionRepository suggestionRepository = getSuggestionRepository();
      Suggestion actual = suggestionRepository.getById(suggestion.getId());
      suggestionRepository.delete(actual);
      return true;
    });
  }

  @Override
  public boolean containsAll(final Collection<?> suggestions) {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    return actual.persistedSuggestions().containsAll(suggestions);
  }

  @Override
  public boolean addAll(final Collection<? extends Suggestion> suggestions) {
    Transaction.performInOne((Process<Void>) () -> {
      final SuggestionRepository suggestionRepository = getSuggestionRepository();
      for (Suggestion suggestion : suggestions) {
        SuggestionBox actual =
            SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
        suggestion.setSuggestionBox(actual);
        actual.persistedSuggestions().add(suggestion);
        suggestionRepository.save(suggestion);
      }
      return null;
    });
    return true;
  }

  @Override
  public boolean removeAll(final Collection<?> theSuggestions) {
    return Transaction.performInOne(() -> {
      final Collection<Suggestion> suggestions = (Collection<Suggestion>) theSuggestions;
      final SuggestionRepository suggestionRepository = getSuggestionRepository();
      Boolean changed = false;
      for (Suggestion suggestion : suggestions) {
        Suggestion actual = suggestionRepository.getById(suggestion.getId());
        if (suggestion.getSuggestionBox().equals(suggestionBox) &&
            (actual.getValidation().isInDraft() || actual.
                getValidation().isRefused())) {
          suggestionRepository.delete(actual);
          changed = true;
        }
      }
      return changed;
    });
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    SuggestionBox actual =
        SuggestionBox.getByComponentInstanceId(suggestionBox.getComponentInstanceId());
    removeAll(actual.persistedSuggestions());
  }

  /**
   * Gets the suggestion with the specified identifier from the suggestions of the suggestion
   * box.
   * @param suggestionId the unique identifier of the suggestion to get.
   * @return the suggestion matching the specified identifier or NONE if no such suggestion exists
   * in the suggestions of the suggestion box.
   */
  public Suggestion get(String suggestionId) {
    Suggestion suggestion = Suggestion.NONE;
    SuggestionCriteria criteria =
        SuggestionCriteria.from(suggestionBox).identifierIsOneOf(suggestionId);
    List<Suggestion> suggestions =
        getSuggestionRepository().findByCriteria(criteria.withWysiwygContent());
    if (suggestions.size() == 1) {
      suggestion = suggestions.get(0);
    }
    return suggestion;
  }

  /**
   * Finds the list of suggestions that are in draft and which the creator is those specified.
   * @param user the creator of the returned suggestions.
   * @return the list of suggestions as described above and ordered by ascending last update date.
   */
  public List<Suggestion> findInDraftFor(final User user) {
    SuggestionCriteria criteria = SuggestionCriteria.from(suggestionBox).createdBy(user).
        statusIsOneOf(DRAFT).orderedBy(LAST_UPDATE_DATE_ASC);
    return getSuggestionRepository().findByCriteria(criteria);
  }

  /**
   * Finds the list of suggestions that are out of draft and which the creator is those specified.
   * @param user the creator of the returned suggestions.
   * @return the list of suggestions as described above and ordered by ascending last update date.
   */
  public List<Suggestion> findOutOfDraftFor(final User user) {
    SuggestionCriteria criteria = SuggestionCriteria.from(suggestionBox).createdBy(user).
        statusIsOneOf(REFUSED, PENDING_VALIDATION, VALIDATED).orderedBy(LAST_UPDATE_DATE_DESC);
    return getSuggestionRepository().findByCriteria(criteria);
  }

  /**
   * Finds the list of suggestions that are published and which the creator is those specified.
   * @param @param user the creator of the returned suggestions.
   * @return the list of suggestions as described above and ordered by ascending last update date.
   */
  public List<Suggestion> findPublishedFor(final User user) {
    SuggestionCriteria criteria = SuggestionCriteria.from(suggestionBox).createdBy(user).
        statusIsOneOf(VALIDATED).orderedBy(LAST_UPDATE_DATE_DESC);
    return getSuggestionRepository().findByCriteria(criteria);
  }

  /**
   * Finds the list of suggestions that are pending validation.
   * @return the list of suggestions as described above and ordered by ascending last update date.
   */
  public List<Suggestion> findPendingValidation() {
    return findInStatus(PENDING_VALIDATION);
  }

  /**
   * Finds the list of suggestions that are published (validated status).
   * @return the list of suggestions as described above and ordered by descending validation
   * date.
   */
  public List<Suggestion> findPublished() {
    SuggestionCriteria criteria = SuggestionCriteria.from(suggestionBox).
        statusIsOneOf(VALIDATED).orderedBy(VALIDATION_DATE_DESC);
    return getSuggestionRepository().findByCriteria(criteria);
  }

  /**
   * Publishes from the specified suggestion box the specified suggestion.
   * <p/>
   * The publication of a suggestion consists in changing its status from DRAFT to
   * PENDING_VALIDATION and sending a notification to the moderator if the updater is at most a
   * writer on the suggestion box.
   * <p/>
   * If the suggestion doesn't exist in the suggestion box, then nothing is done.
   * @param suggestion the suggestion to publish.
   * @return the suggestion updated.
   */
  public Suggestion publish(final Suggestion suggestion) {
    // Persisting the publishing.
    final SuggestionRepository suggestionRepository = getSuggestionRepository();
    Pair<Suggestion, Boolean> result =
        Transaction.performInOne(() -> {
          boolean triggerNotif = false;
          Suggestion actual = get(suggestion.getId());
          if (actual.getValidation().isInDraft() || actual.getValidation().isRefused()) {
            User updater = suggestion.getLastUpdater();
            SilverpeasRole highestUserRole = suggestionBox.getHighestUserRole(updater);
            if (highestUserRole.isGreaterThanOrEquals(SilverpeasRole.WRITER)) {
              ContributionValidation validation = actual.getValidation();
              if (highestUserRole.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
                validation.setStatus(ContributionStatus.VALIDATED);
                validation.setDate(new Date());
                validation.setValidator(updater);
              } else {
                validation.setStatus(ContributionStatus.PENDING_VALIDATION);
              }
              suggestionRepository.save(actual);
              triggerNotif = true;
            }
          }
          return Pair.of(actual, triggerNotif);
        });

    // Sending notification after the persistence is successfully committed.
    Suggestion updatedSuggestion = result.getLeft();
    if (result.getRight()) {
      switch (updatedSuggestion.getValidation().getStatus()) {
        case PENDING_VALIDATION:
          UserNotificationHelper
              .buildAndSend(new SuggestionPendingValidationUserNotification(updatedSuggestion));
          break;
        case VALIDATED:
          suggestionRepository.index(updatedSuggestion);
          UserNotificationHelper
              .buildAndSend(new SuggestionBoxSubscriptionUserNotification(updatedSuggestion));
          break;
        default:
          break;
      }
    }
    return updatedSuggestion;
  }

  /**
   * Validates the specified suggestion in the current suggestion box with the specified
   * validation information.
   * <p/>
   * The publication of a suggestion consists in changing its status to VALIDATED or REFUSED
   * and sending a notification to the creator in order to inform him about the validation
   * result.
   * <p/>
   * If the suggestion doesn't exist in the suggestion box, then nothing is done.
   * @param suggestion the suggestion to validate.
   * @param validation the validation information.
   * @return the updated suggestion.
   */
  public Suggestion validate(final Suggestion suggestion, final ContributionValidation validation) {
    // Persisting the validation.
    final SuggestionRepository suggestionRepository = getSuggestionRepository();
    Pair<Suggestion, Boolean> result =
        Transaction.performInOne(() -> {
          boolean triggerNotif = false;
          Suggestion actual = get(suggestion.getId());
          if (actual.getValidation().isPendingValidation()) {
            User updater = suggestion.getLastUpdater();
            SilverpeasRole highestUserRole = suggestionBox.getHighestUserRole(updater);
            if (highestUserRole.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
              ContributionValidation actualValidation = actual.getValidation();
              actualValidation.setStatus(validation.getStatus());
              actualValidation.setComment(validation.getComment());
              actualValidation.setDate(new Date());
              actualValidation.setValidator(updater);
            }
            suggestionRepository.save(actual);
            triggerNotif = true;
          }
          return Pair.of(actual, triggerNotif);
        });

    // Sending notification(s) after the persistence is successfully committed.
    Suggestion updatedSuggestion = result.getLeft();
    if (result.getRight()) {
      switch (updatedSuggestion.getValidation().getStatus()) {
        case VALIDATED:
          suggestionRepository.index(updatedSuggestion);
          UserNotificationHelper
              .buildAndSend(new SuggestionBoxSubscriptionUserNotification(updatedSuggestion));
          UserNotificationHelper
              .buildAndSend(new SuggestionValidationUserNotification(updatedSuggestion));
          break;
        case REFUSED:
          UserNotificationHelper
              .buildAndSend(new SuggestionValidationUserNotification(updatedSuggestion));
          break;
        default:
          break;
      }
    }
    return updatedSuggestion;
  }

  /**
   * Finds the list of suggestions that are in the specified statuses. The suggestions are ordered
   * by their status and for each status by their modification date.
   * This method is a convenient one to get suggestions of different statuses.
   * @return a list of suggestions ordered by their status and by their modification date.
   */
  public List<Suggestion> findInStatus(ContributionStatus... statuses) {
    SuggestionCriteria criteria = SuggestionCriteria.from(suggestionBox).
        statusIsOneOf(statuses).orderedBy(STATUS_ASC, LAST_UPDATE_DATE_ASC);
    return getSuggestionRepository().findByCriteria(criteria);
  }

  /**
   * Finds the list of all the suggestions that are proposed by the specified user. The
   * suggestions are ordered by status and for each status by modification date.
   * @param author the author of the asked suggestions.
   * @return a list of suggestions ordered by status and by modification date. The list is empty
   * if the user has not proposed any suggestions.
   */
  public List<Suggestion> findAllProposedBy(final User author) {
    SuggestionCriteria criteria = SuggestionCriteria.from(suggestionBox).
        createdBy(author).orderedBy(STATUS_ASC, LAST_UPDATE_DATE_ASC);
    return getSuggestionRepository().findByCriteria(criteria);
  }

  /**
   * Indexes all the published suggestions in this collection.
   */
  public void index() {
    SuggestionRepository suggestionRepository = getSuggestionRepository();
    List<Suggestion> suggestions = suggestionRepository
        .findByCriteria(SuggestionCriteria.from(suggestionBox).statusIsOneOf(VALIDATED));
    for (Suggestion suggestion : suggestions) {
      suggestionRepository.index(suggestion);
    }
  }

  private SuggestionRepository getSuggestionRepository() {
    return SuggestionRepository.get();
  }
}
