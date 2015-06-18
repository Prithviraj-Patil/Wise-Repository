package edu.ucla.wise.selenium;

import java.io.IOException;

import org.junit.Test;

import edu.ucla.wise.web.WiseWebRequester;

/**
 * TODO: This test is incomplete
 * 
 * @author pdessai
 * 
 */
public class WebRequestTest {
    @Test
    public void webRequestTest() throws IOException {
        // HTTP GET request

        String url = "http://localhost:8080/WiseStudySpaceWizard/getParameters?studySpaceName=all";

        WiseWebRequester wr = new WiseWebRequester(url);
        System.out.println(wr.getStudySpaceParameters("password"));
    }
}
