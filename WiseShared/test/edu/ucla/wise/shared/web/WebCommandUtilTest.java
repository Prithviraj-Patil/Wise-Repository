package edu.ucla.wise.shared.web;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class WebCommandUtilTest {

    @Test
    public void testWebCommandUtil() throws UnsupportedEncodingException {
        String plain = "RELOAD_STUDY_SPACES";
        String encryptionKey = "password";
        String encodedEncrypted = WebCommandUtil.getEncodedEncrypted(plain, encryptionKey);
        String decodedDecrypted = WebCommandUtil.getDecodedDecrypted(encodedEncrypted, encryptionKey);
        assertEquals("RELOAD_STUDY_SPACES", decodedDecrypted);

        System.out.println(WebCommandUtil.getDecodedDecrypted("C5XwFgdSMHaPMD8ZlFVEB78IL256XZ3Spsm0r%2BVs%2FTA%3D",
                encryptionKey));
    }

    @Test
    public void testReloadingStudySpacesUrl() throws UnsupportedEncodingException {
        String url = WebCommandUtil.getUrlStringForReloadingStudies("http://localhost:8080", "WISE", "password");
        String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
        String commandPrefix = "http://localhost:8080/WISE/WebCommand?command=";
        int lengthOfCommandPrefex = commandPrefix.length();
        String commandString = decodedUrl.substring(lengthOfCommandPrefex);
        String decodedCommand = WebCommandUtil.getDecrypted(commandString, "password");
        assertEquals("RELOAD_STUDY_SPACES", decodedCommand);
    }
}
