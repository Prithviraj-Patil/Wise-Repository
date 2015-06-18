package edu.ucla.wise.persistence.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.ucla.wise.persistence.data.TextAnswer.TextAnswerDAO;
import edu.ucla.wise.persistence.invitee.Invitee;
import edu.ucla.wise.persitence.invitee.InviteeTest.DatabaseProperties;
import edu.ucla.wise.shared.persistence.HibernateConfiguration;
import edu.ucla.wise.shared.persistence.HibernateUtil;
import edu.ucla.wise.shared.properties.DataBaseProperties;

/**
 * @author pdessai
 */
public class TextAnswersTest {
    public final List<Class<? extends Object>> ANNOTATED_CLASSES = ImmutableList.of(Invitee.class, TextAnswer.class);

    private HibernateUtil getHibernateUtil() {

        DataBaseProperties databaseProperties = new DatabaseProperties();
        HibernateConfiguration hibernateConfig = new HibernateConfiguration(databaseProperties, this.ANNOTATED_CLASSES);
        return new HibernateUtil(hibernateConfig);
    }

    @Test
    public void testTextAnswers() {

        HibernateUtil hibernateUtil = this.getHibernateUtil();
        hibernateUtil.beginTransaction();

        TextAnswerDAO textAnswerDAO = new TextAnswerDAO(hibernateUtil);

        List<TextAnswer> allAnswers = textAnswerDAO.findAll();

        for (TextAnswer textAnswer : allAnswers) {
            textAnswerDAO.remove(textAnswer);
        }

        TextAnswer textAnswer1 = new TextAnswer(1, 1, "firstQuestion", "firstAnswer1");
        TextAnswer textAnswer2 = new TextAnswer(1, 2, "firstQuestion", "firstAnswer2");
        TextAnswer textAnswer3 = new TextAnswer(1, 1, "secondQuestion", "secondAnswer1");
        TextAnswer textAnswer4 = new TextAnswer(1, 2, "secondQuestion", "secondAnswer2");
        TextAnswer textAnswer5 = new TextAnswer(1, 1, "thirdQuestion", "thirdAnswer1");
        TextAnswer textAnswer6 = new TextAnswer(1, 2, "thirdQuestion", "thirdAnswer2");

        Set<TextAnswer> firstInviteeAnswers = new HashSet<>();
        firstInviteeAnswers.add(textAnswer1);
        firstInviteeAnswers.add(textAnswer3);
        firstInviteeAnswers.add(textAnswer5);
        Set<TextAnswer> secondInviteeAnswers = new HashSet<>();
        secondInviteeAnswers.add(textAnswer2);
        secondInviteeAnswers.add(textAnswer4);
        secondInviteeAnswers.add(textAnswer6);

        textAnswerDAO.save(firstInviteeAnswers);
        textAnswerDAO.save(secondInviteeAnswers);
        hibernateUtil.commitTransaction();

        hibernateUtil.beginTransaction();
        // find all answers for first invitee
        TextAnswer example = TextAnswer.getEmptyExample();
        example.setSurvey(1);
        example.setInvitee(1);
        Collection<TextAnswer> results = textAnswerDAO.findByExample(example);

        assertEquals(results.size(), 3);
        StringBuilder firstInviteeAnswersString = new StringBuilder();
        for (TextAnswer result : results) {
            firstInviteeAnswersString.append(result.getAnswer());
        }

        assertTrue(firstInviteeAnswersString.indexOf("firstAnswer1") != -1);
        assertTrue(firstInviteeAnswersString.indexOf("secondAnswer1") != -1);
        assertTrue(firstInviteeAnswersString.indexOf("thirdAnswer1") != -1);

        // find all answers for second invitee
        TextAnswer example2 = TextAnswer.getEmptyExample();
        example2.setSurvey(1);
        example2.setInvitee(2);
        Collection<TextAnswer> results2 = textAnswerDAO.findByExample(example2);

        assertEquals(results2.size(), 3);
        StringBuilder secondInviteeAnswersString = new StringBuilder();
        for (TextAnswer result : results2) {
            secondInviteeAnswersString.append(result.getAnswer());
        }

        assertTrue(secondInviteeAnswersString.indexOf("firstAnswer2") != -1);
        assertTrue(secondInviteeAnswersString.indexOf("secondAnswer2") != -1);
        assertTrue(secondInviteeAnswersString.indexOf("thirdAnswer2") != -1);

    }

}
