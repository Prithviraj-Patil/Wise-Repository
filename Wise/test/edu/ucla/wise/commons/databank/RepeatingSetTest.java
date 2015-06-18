package edu.ucla.wise.commons.databank;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.ucla.wise.persistence.data.Answer;

public class RepeatingSetTest {

    int user1 = 103984;
    int user2 = 103455;

    String survey1 = "survey1";
    String survey2 = "survey2";

    // assume that the same repeat set name is used in both surveys
    String repeatingSetName = "repeat_set_testing";
    String question1 = "Q1";
    String question2 = "Q2";

    String jsonResponse = "[{\"itemSetName\":\"repeat_set_testing\",\"instanceName\":\"instance1\",\"answers\":{\"Q1\":{\"answer\":\"Answer to question 1\",\"type\":\"TEXT\"}}}]";

    /**
     * Scenario: There are two surveys in a study space. User 1 and User 2 are
     * invited to both surveys. Both surveys have repeating items in them. User
     * 1 and User 2 store some answers, and then later retrieve them.
     */

    @Test
    public void repeatingSetTest() {

        UserDataStorer uds = new UserDataStorer(new MockDataBank());
        ResultDataProvider rdp = new ResultDataProvider(new MockDataBank());

        // check if empty
        String u1s1 = rdp.getAnswersInRepeatingSetForInvitee(this.survey1, this.user1, this.repeatingSetName);
        String u1s2 = rdp.getAnswersInRepeatingSetForInvitee(this.survey2, this.user1, this.repeatingSetName);
        String u2s1 = rdp.getAnswersInRepeatingSetForInvitee(this.survey1, this.user2, this.repeatingSetName);
        String u2s2 = rdp.getAnswersInRepeatingSetForInvitee(this.survey2, this.user2, this.repeatingSetName);

        assertEquals("[]", u1s1);
        assertEquals("[]", u1s2);
        assertEquals("[]", u2s1);
        assertEquals("[]", u2s2);

        // insert data
        uds.insertRepeatSetInstance(this.survey1, "" + this.user1, this.repeatingSetName, "instance1",
                this.getAnswersGivenByUser());
        uds.insertRepeatSetInstance(this.survey2, "" + this.user1, this.repeatingSetName, "instance1",
                this.getAnswersGivenByUser());
        uds.insertRepeatSetInstance(this.survey1, "" + this.user2, this.repeatingSetName, "instance1",
                this.getAnswersGivenByUser());
        uds.insertRepeatSetInstance(this.survey2, "" + this.user2, this.repeatingSetName, "instance1",
                this.getAnswersGivenByUser());

        // check data
        String u1s1_1 = rdp.getAnswersInRepeatingSetForInvitee(this.survey1, this.user1, this.repeatingSetName);
        String u1s2_1 = rdp.getAnswersInRepeatingSetForInvitee(this.survey2, this.user1, this.repeatingSetName);
        String u2s1_1 = rdp.getAnswersInRepeatingSetForInvitee(this.survey1, this.user2, this.repeatingSetName);
        String u2s2_1 = rdp.getAnswersInRepeatingSetForInvitee(this.survey2, this.user2, this.repeatingSetName);
        assertEquals(this.jsonResponse, u1s1_1);
        assertEquals(this.jsonResponse, u1s2_1);
        assertEquals(this.jsonResponse, u2s1_1);
        assertEquals(this.jsonResponse, u2s2_1);

        // delete data

        uds.deleteRowFromTable("" + this.user1, this.repeatingSetName, "instance1", this.survey1);
        uds.deleteRowFromTable("" + this.user1, this.repeatingSetName, "instance1", this.survey2);
        uds.deleteRowFromTable("" + this.user2, this.repeatingSetName, "instance1", this.survey1);
        uds.deleteRowFromTable("" + this.user2, this.repeatingSetName, "instance1", this.survey2);

        // check if empty
        String u1s1_2 = rdp.getAnswersInRepeatingSetForInvitee(this.survey1, this.user1, this.repeatingSetName);
        String u1s2_2 = rdp.getAnswersInRepeatingSetForInvitee(this.survey2, this.user1, this.repeatingSetName);
        String u2s1_2 = rdp.getAnswersInRepeatingSetForInvitee(this.survey1, this.user2, this.repeatingSetName);
        String u2s2_2 = rdp.getAnswersInRepeatingSetForInvitee(this.survey2, this.user2, this.repeatingSetName);

        assertEquals("[]", u1s1_2);
        assertEquals("[]", u1s2_2);
        assertEquals("[]", u2s1_2);
        assertEquals("[]", u2s2_2);

    }

    @Test
    public void repeatSetLoadTest() {

        long beginTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            this.repeatingSetTest();
        }

        long endTimeMillis = System.currentTimeMillis();

        System.out.println("Time taken: " + (endTimeMillis - beginTimeMillis));

    }

    private Map<String, Answer> getAnswersGivenByUser() {

        Map<String, Answer> answers = new HashMap<>();
        answers.put(this.question1, new Answer("Answer to question 1", Answer.Type.TEXT));
        answers.put(this.question2, new Answer(1, Answer.Type.INTEGER));

        return answers;

    }
}
