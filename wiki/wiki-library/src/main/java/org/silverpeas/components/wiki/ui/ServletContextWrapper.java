/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.wiki.ui;

import org.silverpeas.components.wiki.SilverWikiEngine;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper of the {@link ServletContext} that takes into account the way the JSP files are located
 * in Silverpeas; that is relative to the <code>wiki/jsp/</code> directory.
 * <p>
 * The problem with some of the JSPWiki's components is that the access to the JSP files can be
 * hardcoded! One of the example is the access of the templates by the
 * {@link org.apache.wiki.ui.TemplateManager} instances. By wrapping the {@link ServletContext}
 * class we ensures the accesses of the JSPWiki's resources is done relative to the
 * <code>wiki/jsp</code> directory when they are done through a {@link ServletContext} object. This
 * way to circumvent the hardcoded access of JSPWIki resources allow us to avoid to rewrite the
 * implementation of some of the concerned components with the details of their implementation
 * (details that can change over the versions of JSPWiki).
 * </p>
 * @author mmoquillon
 */
public class ServletContextWrapper implements ServletContext {

  private final ServletContext servletContext;

  public ServletContextWrapper(final ServletContext context) {
    this.servletContext = context;
  }

  @Override
  public String getContextPath() {
    return servletContext.getContextPath();
  }

  @Override
  public ServletContext getContext(final String uripath) {
    return servletContext.getContext(uripath);
  }

  @Override
  public int getMajorVersion() {
    return servletContext.getMajorVersion();
  }

  @Override
  public int getMinorVersion() {
    return servletContext.getMinorVersion();
  }

  @Override
  public int getEffectiveMajorVersion() {
    return servletContext.getEffectiveMajorVersion();
  }

  @Override
  public int getEffectiveMinorVersion() {
    return servletContext.getEffectiveMinorVersion();
  }

  @Override
  public String getMimeType(final String file) {
    return servletContext.getMimeType(file);
  }

  @Override
  public Set<String> getResourcePaths(final String path) {
    return servletContext.getResourcePaths(path);
  }

  @Override
  public URL getResource(final String path) throws MalformedURLException {
    return servletContext.getResource(fixWikiPath(path));
  }

  @Override
  public InputStream getResourceAsStream(final String path) {
    return servletContext.getResourceAsStream(fixWikiPath(path));
  }

  @Override
  public RequestDispatcher getRequestDispatcher(final String path) {
    return servletContext.getRequestDispatcher(path);
  }

  @Override
  public RequestDispatcher getNamedDispatcher(final String name) {
    return servletContext.getNamedDispatcher(name);
  }

  @Override
  public Servlet getServlet(final String name) throws ServletException {
    return servletContext.getServlet(name);
  }

  @Override
  public Enumeration<Servlet> getServlets() {
    return servletContext.getServlets();
  }

  @Override
  public Enumeration<String> getServletNames() {
    return servletContext.getServletNames();
  }

  @Override
  public void log(final String msg) {
    servletContext.log(msg);
  }

  @Override
  public void log(final Exception exception, final String msg) {
    servletContext.log(exception, msg);
  }

  @Override
  public void log(final String message, final Throwable throwable) {
    servletContext.log(message, throwable);
  }

  @Override
  public String getRealPath(final String path) {
    return servletContext.getRealPath(path);
  }

  @Override
  public String getServerInfo() {
    return servletContext.getServerInfo();
  }

  @Override
  public String getInitParameter(final String name) {
    return servletContext.getInitParameter(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return servletContext.getInitParameterNames();
  }

  @Override
  public boolean setInitParameter(final String name, final String value) {
    return servletContext.setInitParameter(name, value);
  }

  @Override
  public Object getAttribute(final String name) {
    return servletContext.getAttribute(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return servletContext.getAttributeNames();
  }

  @Override
  public void setAttribute(final String name, final Object object) {
    servletContext.setAttribute(name, object);
  }

  @Override
  public void removeAttribute(final String name) {
    servletContext.removeAttribute(name);
  }

  @Override
  public String getServletContextName() {
    return servletContext.getServletContextName();
  }

  @Override
  public ServletRegistration.Dynamic addServlet(final String servletName, final String className) {
    return servletContext.addServlet(servletName, className);
  }

  @Override
  public ServletRegistration.Dynamic addServlet(final String servletName, final Servlet servlet) {
    return servletContext.addServlet(servletName, servlet);
  }

  @Override
  public ServletRegistration.Dynamic addServlet(final String servletName,
      final Class<? extends Servlet> servletClass) {
    return servletContext.addServlet(servletName, servletClass);
  }

  @Override
  public <T extends Servlet> T createServlet(final Class<T> clazz) throws ServletException {
    return servletContext.createServlet(clazz);
  }

  @Override
  public ServletRegistration getServletRegistration(final String servletName) {
    return servletContext.getServletRegistration(servletName);
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    return servletContext.getServletRegistrations();
  }

  @Override
  public FilterRegistration.Dynamic addFilter(final String filterName, final String className) {
    return servletContext.addFilter(filterName, className);
  }

  @Override
  public FilterRegistration.Dynamic addFilter(final String filterName, final Filter filter) {
    return servletContext.addFilter(filterName, filter);
  }

  @Override
  public FilterRegistration.Dynamic addFilter(final String filterName,
      final Class<? extends Filter> filterClass) {
    return servletContext.addFilter(filterName, filterClass);
  }

  @Override
  public <T extends Filter> T createFilter(final Class<T> clazz) throws ServletException {
    return servletContext.createFilter(clazz);
  }

  @Override
  public FilterRegistration getFilterRegistration(final String filterName) {
    return servletContext.getFilterRegistration(filterName);
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    return servletContext.getFilterRegistrations();
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    return servletContext.getSessionCookieConfig();
  }

  @Override
  public void setSessionTrackingModes(final Set<SessionTrackingMode> sessionTrackingModes) {
    servletContext.setSessionTrackingModes(sessionTrackingModes);
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    return servletContext.getDefaultSessionTrackingModes();
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    return servletContext.getEffectiveSessionTrackingModes();
  }

  @Override
  public void addListener(final String className) {
    servletContext.addListener(className);
  }

  @Override
  public <T extends EventListener> void addListener(final T t) {
    servletContext.addListener(t);
  }

  @Override
  public void addListener(final Class<? extends EventListener> listenerClass) {
    servletContext.addListener(listenerClass);
  }

  @Override
  public <T extends EventListener> T createListener(final Class<T> clazz) throws ServletException {
    return servletContext.createListener(clazz);
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    return servletContext.getJspConfigDescriptor();
  }

  @Override
  public ClassLoader getClassLoader() {
    return servletContext.getClassLoader();
  }

  @Override
  public void declareRoles(final String... roleNames) {
    servletContext.declareRoles(roleNames);
  }

  @Override
  public String getVirtualServerName() {
    return servletContext.getVirtualServerName();
  }

  private String fixWikiPath(final String path) {
    String fixedPath = path;
    if (path.startsWith("/templates")) {
      SilverWikiEngine engine = SilverWikiEngine.getInstance(this);
      fixedPath = engine.getTemplatePath(path);
    }
    return fixedPath;
  }
}
  