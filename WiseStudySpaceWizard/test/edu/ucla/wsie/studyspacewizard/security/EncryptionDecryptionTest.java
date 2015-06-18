package edu.ucla.wsie.studyspacewizard.security;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Test;

public class EncryptionDecryptionTest {

    @Test
    public void encryptDecryptTest() {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword("password");
        String myEncryptedText = textEncryptor.encrypt("Hello");
        System.out.println(myEncryptedText);
        String escapedHtml = StringEscapeUtils.escapeHtml4(myEncryptedText);
        System.out.println(escapedHtml);
        String unescapedHtml = StringEscapeUtils.unescapeHtml4(escapedHtml);
        System.out.println(unescapedHtml);
        String plainText = textEncryptor.decrypt(myEncryptedText);
        System.out.println(plainText);

    }

}
