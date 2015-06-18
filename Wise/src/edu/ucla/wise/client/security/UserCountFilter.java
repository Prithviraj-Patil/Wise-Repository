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
package edu.ucla.wise.client.security;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.apache.log4j.Logger;

import edu.ucla.wise.emailscheduler.WISE_TimeUtils;

/**
 * Check if user hit counter indicates heavy traffic
 */
@WebFilter("/survey/*")
public class UserCountFilter implements Filter {

    /**
     * The number of requests made to the survey.
     */
    private AtomicInteger userRequestCounter;

    /**
     * Time at which the userRequestCounter was reset.
     */
    private AtomicLong resetTimestamp;

    /**
     * Maximum user requests allowed in a day.
     */
    public static final int MAX_USERS_ALLOWED = 100000;

    /**
     * Log4j logger
     */
    private static final Logger LOGGER = Logger.getLogger(UserCountFilter.class);

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {
        this.userRequestCounter.getAndIncrement();
        if (this.userRequestCounter.get() > MAX_USERS_ALLOWED) {
            Long currentTime = System.currentTimeMillis();
            if (currentTime < (this.resetTimestamp.get() + WISE_TimeUtils.MILLISECONDS_IN_A_DAY)) {
                response.getWriter().println(
                        "Too many users in the system" + "<p> WISE Begin failed </p>"
                                + edu.ucla.wise.commons.SurveyorApplication.initErrorHtmlFoot);
                LOGGER.info("ALERT - Too many users in the system");
                return;
            } else {
                this.resetTimestamp.set(currentTime);
                LOGGER.info("The UserCountFilter timestamp has been reset at " + currentTime);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public final void init(final FilterConfig arg0) throws ServletException {
        this.userRequestCounter = new AtomicInteger(0);
        this.resetTimestamp = new AtomicLong(System.currentTimeMillis());
    }

}
