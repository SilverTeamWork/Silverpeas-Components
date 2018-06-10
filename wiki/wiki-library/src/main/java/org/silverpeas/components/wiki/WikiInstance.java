package org.silverpeas.components.wiki;

import org.apache.wiki.WikiEngine;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.providers.FileSystemProvider;
import org.apache.wiki.util.TextUtil;
import org.silverpeas.core.admin.user.model.User;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * Represents a given Wiki application instance. It gathers information qualifying such a
 * Wiki instance.
 * @author Miguel Moquillon
 */
public class WikiInstance {

  private final String wikiId;
  private final String contextPath;
  private final Properties props;

  WikiInstance(final SilverWikiEngine engine, final String wikiId) {
    Objects.requireNonNull(engine);
    Objects.requireNonNull(wikiId);
    this.contextPath = engine.getServletContext().getContextPath();
    this.wikiId = wikiId;
    this.props = new Properties(engine.getWikiProperties());
    final String workDir = Paths.get(props.getProperty(WikiEngine.PROP_WORKDIR), wikiId).toString();
    final WikiSettings wikiSettings = new WikiSettings(wikiId);
    User user = User.getCurrentRequester();
    props.setProperty(FileSystemProvider.PROP_PAGEDIR, wikiSettings.getWikiPageDirPath());
    props.setProperty(BasicAttachmentProvider.PROP_STORAGEDIR,
        wikiSettings.getWikiAttachmentDirPath());
    props.setProperty(WikiEngine.PROP_WORKDIR, workDir);
    props.setProperty(AttachmentManager.PROP_MAXSIZE,
        String.valueOf(wikiSettings.getAttachmentMaxSize()));
    props.setProperty("jspwiki.defaultprefs.template.language",
        user.getUserPreferences().getLanguage());
  }

  public String getWikiId() {
    return this.wikiId;
  }

  public String getBaseUrl() {
    return contextPath + "/Rwiki/" + wikiId;
  }

  public Properties getProperties() {
    return this.props;
  }

  public String getWorkDir() {
    return TextUtil.getStringProperty( props, WikiEngine.PROP_WORKDIR, null );
  }

  public String getTemplateDir() {
    return TextUtil.getStringProperty( props, WikiEngine.PROP_TEMPLATEDIR, "default" );
  }
}
