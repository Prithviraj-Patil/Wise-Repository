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

package edu.ucla.wise.initializer;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.emailscheduler.EmailScheduler;

/**
 * WiseApplicationInitializer class is used to initialize the classes needed for
 * running the WISE Application.
 * 
 * 
 */
public class WiseApplicationInitializer implements ServletContextListener {

    public static final String WISE_HOME = "WISE_HOME";

    public static final Logger LOGGER = Logger.getLogger(WiseApplicationInitializer.class);

    /**
     * Destroys the email scheduler.
     * 
     * @param arg0
     *            ServletContextEvent.
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        EmailScheduler.destroyScheduler();
    }

    /**
     * Initializes all the needed classes and starts the email scheduler thread.
     * 
     * @param servletContextEvent
     *            ServletContextEvent.
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            LOGGER.info("Wise Application initializing");

            String wiseHome = System.getenv(WISE_HOME);
            if (Strings.isNullOrEmpty(wiseHome)) {
                LOGGER.info("WISE_HOME environment variable is not set");
                return;
            }

            String rootFolderPath = servletContextEvent.getServletContext().getRealPath("/");
            WiseProperties properties = new WiseProperties(wiseHome + "/wise.properties", "WISE");
            String contextPath = servletContextEvent.getServletContext().getContextPath();

            // All initializing statements below
            WISEApplication.initialize(contextPath, rootFolderPath, properties);
            // end of initializing statements

            LOGGER.info("Wise Application initialized");
        } catch (IOException e) {
            LOGGER.error("IO Exception while initializing", e);
        } catch (IllegalStateException e) {
            LOGGER.error("The admin or the survey app was not " + "initialized, WISE application cannot start", e);
        }
    }

}