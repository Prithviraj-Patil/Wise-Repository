package edu.ucla.wise.client;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class FreemarkerSurveyGenerateTest {

    @Test
    public void testFreemarkerSurveyGeneration() throws IOException, TemplateException {
        Configuration cfg = this.configureFreemarker();

        Template template = cfg.getTemplate("survey_template.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(this.getMapValues(), new PrintWriter(stringWriter));
        String response = stringWriter.toString();
        assertTrue(response.contains("someUserId"));
    }

    private Map<String, Object> getMapValues() {
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("sharedUrl", "some/url");
        input.put("userId", "someUserId");
        input.put("pageHtml", "somePageHtml");
        input.put("progressBarHtml", "someProgressBarHtml");
        return input;
    }

    private Configuration configureFreemarker() throws IOException {
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File("WebContent/survey/templates"));

        cfg.setIncompatibleImprovements(new Version(1, 0, 0));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return cfg;
    }
}
