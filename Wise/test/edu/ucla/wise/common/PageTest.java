package edu.ucla.wise.common;

import java.util.ArrayList;

import org.junit.Test;

import edu.ucla.wise.commons.Page;
import edu.ucla.wise.commons.RepeatingItemSet;
import edu.ucla.wise.commons.UserAnswers;

public class PageTest {

    @Test
    public void pageRenderTest() {

        /**
         * public Page(String id, String title, String instructions, Survey
         * survey, PageItem[] items, String[] allFieldNames, char[]
         * allValueTypes, ArrayList<RepeatingItemSet> repeatingItems, boolean
         * finalPage, String nextPage, Condition condition)
         */
        Page page = new Page("id", "title", "instructions", null, null, null, null, new ArrayList<RepeatingItemSet>(),
                false, null, null);

        System.out.println(page.renderPage(new DemoPageUser()));

    }

    class DemoPageUser implements UserAnswers {

        @Override
        public String getJSValues() {
            return "[demo,values]";
        }

        @Override
        public Integer getFieldValue(String preField) {
            return 0;
        }

    }
}
