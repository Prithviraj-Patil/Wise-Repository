/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import edu.ucla.wise.commons.SanityCheck;

/**
 * Filter to check request parameters send to admin and client. Checks all
 * parameters to avoid code duplication in individual servlets.
 * 
 * @author pdessai
 * 
 */
@WebFilter("/*")
public class RequestParametersCheckerFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(RequestParametersCheckerFilter.class);

    private final Set<String> noFilterNameSet = new HashSet<>();

    @Override
    public void destroy() {
        LOGGER.info("RequestParamtersCheckerFilter destroyed");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        try {

            LOGGER.debug("RequestParametersCheckerFilter: " + httpServletRequest.getRequestURI());
            Map<String, String[]> requestParametersMap = request.getParameterMap();
            LOGGER.debug(new Gson().toJson(requestParametersMap));
            LOGGER.debug("Now checking all request parameters...");
            for (String parameterName : requestParametersMap.keySet()) {
                if (this.noFilterNameSet.contains(parameterName)) {
                    LOGGER.debug("Skipping check for parameter '" + parameterName + "'");
                } else {
                    for (String parameterValue : requestParametersMap.get(parameterName)) {
                        if (SanityCheck.sanityCheck(parameterValue)) {
                            // Not a sanitized value
                            LOGGER.error("RequestParametersCheckerFilter:" + parameterValue);
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()
                                    + "/admin/error_pages/sanity_error.html");
                            return;
                        }
                    }
                }
            }
            LOGGER.debug("Request parameters check complete");
            filterChain.doFilter(request, response);
            return;
        } catch (IOException e) {
            LOGGER.error(e);
        } catch (ServletException e) {
            LOGGER.error(e);
        } catch (RuntimeException e) {
            LOGGER.error(e);
        }
        try {
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/admin/error_pages/error.htm");
        } catch (IOException e1) {
            LOGGER.error(e1);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.noFilterNameSet.add("command");
        LOGGER.info("RequestParametersCheckerFilter initialized");
    }

}
