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
package edu.ucla.wise.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jasypt.util.text.BasicTextEncryptor;

import com.google.gson.Gson;

import edu.ucla.wise.shared.web.WebRequester;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/**
 * This class provides all the methods to access external urls to get data. One
 * WebRequester per URL.
 */
public class WiseWebRequester extends WebRequester {

    /**
     * The Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(WiseWebRequester.class);

    public WiseWebRequester(String url) throws MalformedURLException {
        super(url);
    }

    /**
     * Get a map of all StudySpaceParameters from the
	 * StudySpaceParametersWizard.
	 * 
	 * @param password
	 *            required to decrypt returned data.
	 * @return
	 * @throws IOException
	 */
	public final Map<String, StudySpaceParameters> getStudySpaceParameters(String password)
			throws IOException {
		String response = this.getResponseUsingGET();
		LOGGER.debug(response.toString());
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(password);
		String decryptedString = textEncryptor.decrypt(response.toString());
		LOGGER.debug("'" + decryptedString + "'");
		Gson gson = new Gson();
		List<Map<String, String>> parameters = gson.fromJson(decryptedString, List.class);
		LOGGER.debug(parameters);
		Map<String, StudySpaceParameters> sspMap = new HashMap<>();
		for (Map<String, String> params : parameters) {
			StudySpaceParameters ssp = new StudySpaceParameters(params);
			sspMap.put(ssp.getName(), ssp);
		}
		return sspMap;
	}
}
