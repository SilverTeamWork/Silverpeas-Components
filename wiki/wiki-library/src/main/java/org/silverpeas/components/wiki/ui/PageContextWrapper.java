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

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.ErrorData;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

/**
 * The page context to use by the {@link WikiTemplateManager} objects and that takes into account
 * the location of the JSPWiki's templates in Silverpeas. Indeed, the path of the templates is
 * hardcoded in JSPWiki and we have to circumvent that.
 * @author mmoquillon
 */
public class PageContextWrapper extends PageContext {

  private final PageContext pageCtx;

  public PageContextWrapper(final PageContext pageContext) {
    this.pageCtx = pageContext;
  }

  @Override
  public void initialize(final Servlet servlet, final ServletRequest request,
      final ServletResponse response, final String errorPageURL, final boolean needsSession,
      final int bufferSize, final boolean autoFlush)
      throws IOException, IllegalStateException, IllegalArgumentException {
    pageCtx.initialize(servlet, request, response, errorPageURL, needsSession, bufferSize,
        autoFlush);
  }

  @Override
  public void release() {
    pageCtx.release();
  }

  @Override
  public HttpSession getSession() {
    return pageCtx.getSession();
  }

  @Override
  public Object getPage() {
    return pageCtx.getPage();
  }

  @Override
  public ServletRequest getRequest() {
    return pageCtx.getRequest();
  }

  @Override
  public ServletResponse getResponse() {
    return pageCtx.getResponse();
  }

  @Override
  public Exception getException() {
    return pageCtx.getException();
  }

  @Override
  public ServletConfig getServletConfig() {
    return pageCtx.getServletConfig();
  }

  @Override
  public ServletContext getServletContext() {
    return new ServletContextWrapper(pageCtx.getServletContext());
  }

  @Override
  public void forward(final String relativeUrlPath) throws ServletException, IOException {
    pageCtx.forward(relativeUrlPath);
  }

  @Override
  public void include(final String relativeUrlPath) throws ServletException, IOException {
    pageCtx.include(relativeUrlPath);
  }

  @Override
  public void include(final String relativeUrlPath, final boolean flush)
      throws ServletException, IOException {
    pageCtx.include(relativeUrlPath, flush);
  }

  @Override
  public void handlePageException(final Exception e) throws ServletException, IOException {
    pageCtx.handlePageException(e);
  }

  @Override
  public void handlePageException(final Throwable t) throws ServletException, IOException {
    pageCtx.handlePageException(t);
  }

  @Override
  public BodyContent pushBody() {
    return pageCtx.pushBody();
  }

  @Override
  public ErrorData getErrorData() {
    return pageCtx.getErrorData();
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    pageCtx.setAttribute(name, value);
  }

  @Override
  public void setAttribute(final String name, final Object value, final int scope) {
    pageCtx.setAttribute(name, value, scope);
  }

  @Override
  public Object getAttribute(final String name) {
    return pageCtx.getAttribute(name);
  }

  @Override
  public Object getAttribute(final String name, final int scope) {
    return pageCtx.getAttribute(name, scope);
  }

  @Override
  public Object findAttribute(final String name) {
    return pageCtx.findAttribute(name);
  }

  @Override
  public void removeAttribute(final String name) {
    pageCtx.removeAttribute(name);
  }

  @Override
  public void removeAttribute(final String name, final int scope) {
    pageCtx.removeAttribute(name, scope);
  }

  @Override
  public int getAttributesScope(final String name) {
    return pageCtx.getAttributesScope(name);
  }

  @Override
  public Enumeration<String> getAttributeNamesInScope(final int scope) {
    return pageCtx.getAttributeNamesInScope(scope);
  }

  @Override
  public JspWriter getOut() {
    return pageCtx.getOut();
  }

  @Override
  public ExpressionEvaluator getExpressionEvaluator() {
    return pageCtx.getExpressionEvaluator();
  }

  @Override
  public VariableResolver getVariableResolver() {
    return pageCtx.getVariableResolver();
  }

  @Override
  public ELContext getELContext() {
    return pageCtx.getELContext();
  }

  @Override
  public JspWriter pushBody(final Writer writer) {
    return pageCtx.pushBody(writer);
  }

  @Override
  public JspWriter popBody() {
    return pageCtx.popBody();
  }
}
  