package edu.ucla.wise.shared.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class WebRequester {

	/**
	 * The URL to connect to.
	 */
	private final URL url;

	/**
	 * The Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(WebRequester.class);

	/**
	 * One WebRequester per url provided.
	 * 
	 * @param url
	 * @throws MalformedURLException
	 */
	public WebRequester(final String url) throws MalformedURLException {
		this.url = new URL(url);
	}

	/**
	 * Use get request to make an http call to the web service.
	 * 
	 * @return the response from the web service
	 * @throws IOException
	 */
	public final String getResponseUsingGET() throws IOException {
		HttpURLConnection con = (HttpURLConnection) this.url.openConnection();

		con.setRequestMethod("GET");

		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		this.getLogger().debug("\nSending 'GET' request to URL : " + this.url);
		this.getLogger().debug("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	/**
	 * Override this method in subclasses
	 * 
	 * @return
	 */
	protected Logger getLogger() {
		return LOGGER;
	}

}
