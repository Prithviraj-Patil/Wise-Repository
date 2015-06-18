package edu.ucla.wise.persistence.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserDataTest {

    @Test
    public void userDataTest() {
        UserData userData = new UserData(1, 1);

        userData.addAnswer("question1", "my name is bob");
        userData.addAnswer("question1", "my name is charlie");
        userData.addAnswer("question2", 1);
        userData.addAnswer("question2", 2);

        assertEquals(userData.getTextAnswer("question1"), "my name is charlie");
        assertEquals(userData.getNumericAnswer("question2"), 2);

    }

}
