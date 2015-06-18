package edu.ucla.wise.commons.databank;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.ucla.wise.persistence.data.WiseTables;
import edu.ucla.wise.utils.SQLTemplateUtil;
import edu.ucla.wise.utils.TemplateUtil;
import freemarker.template.Configuration;

public class MockDataBank implements DataBankInterface {

    private static final String STUDY_NAME = "wisedev";

    private Connection connection = null;

    @Override
    public Connection getDBConnection() throws SQLException {
        if (this.connection == null) {
            this.connection = DriverManager.getConnection(getTestConnectionParameterString());
        }
        return this.connection;
    }

    @Override
    public String getStudySpace() {
        return STUDY_NAME;
    }

    @Override
    public WiseTables getWiseTables() {
        return new WiseTables(STUDY_NAME);
    }

    private static String getTestConnectionParameterString() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        } catch (SQLException e) {
            System.out.println("DataBank init Error");
            e.printStackTrace();
        }
        return "jdbc:mysql://localhost:3306/wisedev?user=wisedev&password=password&autoReconnect=true";
    }

    @Override
    public SQLTemplateUtil getSqlTemplateUtil() {
        Configuration cfg = null;
        try {
            cfg = TemplateUtil.createTemplateConfiguration("WebContent/", "admin/sql_templates");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SQLTemplateUtil(cfg);
    }

    // Not completed
    public void clearDatabase() {
        WiseTables wiseTables = this.getWiseTables();
        String[] tablesToClear = { wiseTables.getMainDataText(), wiseTables.getMainDataInteger(),
                wiseTables.getDataRepeatInstanceToQuestionId(), wiseTables.getDataRepeatSetToInstance() };
        String sql = "DELETE FROM ${table} WHERE id=?";

        try {
            Connection con = this.getDBConnection();

            for (String table : tablesToClear) {
                String sqlForTable = sql.replace("${table}", table);
                PreparedStatement stmt = con.prepareStatement(sqlForTable);
                stmt.execute();
                stmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
