package com.cloudcheflabs.iceberg.catalog.rest.filter;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class RequestFilter implements Filter {

  private static Logger LOG = LoggerFactory.getLogger(RequestFilter.class);

  public static final String KEY_USER = "user";
  public static final String KEY_TOKEN = "token";

  private FilterConfig filterConfig = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    // add cors.
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.setHeader("Access-Control-Allow-Methods", "*");
    httpResponse.setHeader("Access-Control-Max-Age", "3600");
    httpResponse.setHeader("Access-Control-Allow-Headers", "*");
    httpResponse.setHeader("Access-Control-Allow-Origin", "*");
    if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
      httpResponse.setStatus(HttpServletResponse.SC_OK);
      chain.doFilter(request, response);
      return;
    }

    String authHeader = httpRequest.getHeader("Authorization");
    if (authHeader != null) {
      String[] headerTokens = authHeader.split(" ");
      String bearer = headerTokens[0];
      String token = headerTokens[1];

      // TODO: validate token.
      boolean isValid = token.equals("valid-token-12345");
      if(isValid) {
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest);
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(httpResponse);
        chain.doFilter(requestWrapper, responseWrapper);
      } else {
        throw new ServletException("Token is not valid!");
      }
    } else {
      throw new ServletException("Authorization header not found!");
    }
  }

  public void destroy() {
    this.filterConfig = null;
  }
}
