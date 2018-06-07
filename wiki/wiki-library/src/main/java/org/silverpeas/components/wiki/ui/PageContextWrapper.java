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
 * @author mmoquillon
 */
public class PageContextWrapper extends PageContext {

  private final PageContext pageContext;

  public PageContextWrapper(final PageContext pageContext) {
    this.pageContext = pageContext;
  }

  @Override
  public void initialize(final Servlet servlet, final ServletRequest request,
      final ServletResponse response, final String errorPageURL, final boolean needsSession,
      final int bufferSize, final boolean autoFlush)
      throws IOException, IllegalStateException, IllegalArgumentException {
    pageContext.initialize(servlet, request, response, errorPageURL, needsSession, bufferSize,
        autoFlush);
  }

  @Override
  public void release() {
    pageContext.release();
  }

  @Override
  public HttpSession getSession() {
    return pageContext.getSession();
  }

  @Override
  public Object getPage() {
    return pageContext.getPage();
  }

  @Override
  public ServletRequest getRequest() {
    return pageContext.getRequest();
  }

  @Override
  public ServletResponse getResponse() {
    return pageContext.getResponse();
  }

  @Override
  public Exception getException() {
    return pageContext.getException();
  }

  @Override
  public ServletConfig getServletConfig() {
    return pageContext.getServletConfig();
  }

  @Override
  public ServletContext getServletContext() {
    return new ServletContextWrapper(pageContext.getServletContext());
  }

  @Override
  public void forward(final String relativeUrlPath) throws ServletException, IOException {
    pageContext.forward(relativeUrlPath);
  }

  @Override
  public void include(final String relativeUrlPath) throws ServletException, IOException {
    pageContext.include(relativeUrlPath);
  }

  @Override
  public void include(final String relativeUrlPath, final boolean flush)
      throws ServletException, IOException {
    pageContext.include(relativeUrlPath, flush);
  }

  @Override
  public void handlePageException(final Exception e) throws ServletException, IOException {
    pageContext.handlePageException(e);
  }

  @Override
  public void handlePageException(final Throwable t) throws ServletException, IOException {
    pageContext.handlePageException(t);
  }

  @Override
  public BodyContent pushBody() {
    return pageContext.pushBody();
  }

  @Override
  public ErrorData getErrorData() {
    return pageContext.getErrorData();
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    pageContext.setAttribute(name, value);
  }

  @Override
  public void setAttribute(final String name, final Object value, final int scope) {
    pageContext.setAttribute(name, value, scope);
  }

  @Override
  public Object getAttribute(final String name) {
    return pageContext.getAttribute(name);
  }

  @Override
  public Object getAttribute(final String name, final int scope) {
    return pageContext.getAttribute(name, scope);
  }

  @Override
  public Object findAttribute(final String name) {
    return pageContext.findAttribute(name);
  }

  @Override
  public void removeAttribute(final String name) {
    pageContext.removeAttribute(name);
  }

  @Override
  public void removeAttribute(final String name, final int scope) {
    pageContext.removeAttribute(name, scope);
  }

  @Override
  public int getAttributesScope(final String name) {
    return pageContext.getAttributesScope(name);
  }

  @Override
  public Enumeration<String> getAttributeNamesInScope(final int scope) {
    return pageContext.getAttributeNamesInScope(scope);
  }

  @Override
  public JspWriter getOut() {
    return pageContext.getOut();
  }

  @Override
  public ExpressionEvaluator getExpressionEvaluator() {
    return pageContext.getExpressionEvaluator();
  }

  @Override
  public VariableResolver getVariableResolver() {
    return pageContext.getVariableResolver();
  }

  @Override
  public ELContext getELContext() {
    return pageContext.getELContext();
  }

  @Override
  public JspWriter pushBody(final Writer writer) {
    return pageContext.pushBody(writer);
  }

  @Override
  public JspWriter popBody() {
    return pageContext.popBody();
  }
}
  