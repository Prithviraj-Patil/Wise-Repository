package edu.ucla.wise.commons.databank;

import java.sql.SQLException;

import org.junit.Test;

public class ConnectionLoadTest {

    @Test
    public void multipleConnectionTest() throws SQLException {
        /*
         * for (int i = 0; i < 1000; i++) { DataBankInterface db = new
         * MockDataBank(); Connection conn = db.getDBConnection();
         * 
         * PreparedStatement stmt =
         * conn.prepareStatement("select * from wisedev.data_text");
         * stmt.execute(); }
         */
    }

}
