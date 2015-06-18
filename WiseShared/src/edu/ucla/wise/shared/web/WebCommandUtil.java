package edu.ucla.wise.shared.web;

import java.io.UnsupportedEncodingException;

import org.jasypt.util.text.BasicTextEncryptor;

public class WebCommandUtil {
    private static final String UTF_8 = "UTF-8";

    public static String getUrlStringForReloadingStudies(String serverUrl, String serverApp, String encryptionKey)
            throws UnsupportedEncodingException {
        String webCommand = "WebCommand";
        String commandParameters = "command=" + getEncodedEncrypted("RELOAD_STUDY_SPACES", encryptionKey);
        return serverUrl + "/" + serverApp + "/" + webCommand + "?" + commandParameters;
    }

    public static String getEncodedEncrypted(String plain, String encryptionKey) throws UnsupportedEncodingException {
        return java.net.URLEncoder.encode(getEncrypted(plain, encryptionKey), UTF_8);
    }

    public static String getDecodedDecrypted(String encrypted, String encryptionKey)
            throws UnsupportedEncodingException {
        return getDecrypted(java.net.URLDecoder.decode(encrypted, UTF_8), encryptionKey);
    }

    public static String getEncrypted(String plain, String encryptionKey) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        return textEncryptor.encrypt(plain).toString();
    }

    public static String getDecrypted(String encrypted, String encryptionKey) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        return textEncryptor.decrypt(encrypted).toString();
    }

}
