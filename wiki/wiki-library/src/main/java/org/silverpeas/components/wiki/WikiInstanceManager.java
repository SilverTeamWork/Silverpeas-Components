package org.silverpeas.components.wiki;

import org.silverpeas.core.cache.service.CacheServiceProvider;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It manages the different Wiki application instances by setting the current accessed Wiki
 * application instance in Silverpeas for the current Wiki processing thread.
 * <p>
 * JSPWiki is designed to be a single Web Application and as such the Wiki engine is set as an
 * attribute of the servlet context. Unfortunately, in Silverpeas several instances of a Wiki
 * application can cohabit and each of them will require then a different Wiki engine,
 * mainly because those engines compute the URLs to access the different JSPWiki's resources for
 * a given Wiki instance.
 * Because it isn't possible to have several Wiki engines while ensuring a multi-threaded
 * computation (there is one single servlet context and hence accesses for different wiki instances
 * can be come in several threads), another way to simulate a different Wiki engine is to have
 * one single Wiki engine (set as an attribute to the servlet context), and a manager of Wiki
 * instances with which the Wiki engine will communicate to known what Wiki instance is being
 * currently accessed.
 * </p>
 * @author Miguel Moquillon
 */
@Singleton
public class WikiInstanceManager {

  private static final String ATTR_WIKIENGINE = "org.apache.wiki.WikiEngine";
  private final Map<String, WikiInstance> wiki = new ConcurrentHashMap<>(5);

  protected WikiInstanceManager() {
    // for CDI
  }

  Optional<WikiInstance> getCurrentWikiInstance() {
    return Optional.ofNullable(CacheServiceProvider.getRequestCacheService()
        .getCache()
        .get(ATTR_WIKIENGINE, WikiInstance.class));
  }

  void setCurrentWikiInstance(final SilverWikiEngine engine, final String wikiId) {
    Objects.requireNonNull(wikiId);
    WikiInstance current = wiki.computeIfAbsent(wikiId, key -> new WikiInstance(engine, key));
    CacheServiceProvider.getRequestCacheService().getCache().put(ATTR_WIKIENGINE, current);
  }

  String getTemplatePath(final String template) {
    Objects.requireNonNull(template);
    return WikiSettings.WIKI_BASE_DIR + template;
  }

  void deleteWikiInstance(final String wikiId) {
    this.wiki.remove(wikiId);
  }

  String getWikiWebPath(final ServletContext context) {
    return context.getContextPath() + "/wiki/jsp";
  }
}
