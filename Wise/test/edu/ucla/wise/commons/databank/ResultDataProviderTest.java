package edu.ucla.wise.commons.databank;

import java.util.List;

import org.junit.Test;

public class ResultDataProviderTest {

    @Test
    public void getDistictQuestionIdsTest() {
        String surveyName = "wisedev";
        int questionLevel = 0;
        ResultDataProvider rdp = new ResultDataProvider(new MockDataBank());
        List<String> questionIds = rdp.getDistinctQuestionIds(surveyName, questionLevel);
        System.out.println(questionIds);
        List<String> inviteeIds = rdp.getDistinctInviteeIdsFromDataTable(surveyName, questionLevel);
        System.out.println(inviteeIds);

        System.out.println(rdp.getInviteeAnswersTable(surveyName, 0));

        System.out.println(rdp.getRepeatSetData(surveyName, "repeat_set_publications"));
    }

}
