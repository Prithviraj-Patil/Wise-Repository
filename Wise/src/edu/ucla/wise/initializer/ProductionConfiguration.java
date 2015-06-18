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
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;
import edu.ucla.wise.web.WiseWebRequester;

/**
 * Class contains configuration when the code is run in production. Provides
 * separation from DevelopmentConfiguration
 */
public class ProductionConfiguration extends WiseConfiguration {

	public ProductionConfiguration(WiseProperties wiseProperties) {
		super(wiseProperties);
	}

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = Logger.getLogger(ProductionConfiguration.class);

	@Override
	public final Map<String, StudySpaceParameters> getStudySpaceParameters() {
		try {
			WiseWebRequester webRequester = new WiseWebRequester(this.getWiseProperties()
					.getStudySpaceWizardParametersUrl());
			return webRequester.getStudySpaceParameters(this.getWiseProperties()
					.getStudySpaceWizardPassword());
		} catch (MalformedURLException e) {
			LOGGER.error(e);
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return null;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public final CONFIG_TYPE getConfigType() {
		return WiseConfiguration.CONFIG_TYPE.PRODUCTION;
	}

}
