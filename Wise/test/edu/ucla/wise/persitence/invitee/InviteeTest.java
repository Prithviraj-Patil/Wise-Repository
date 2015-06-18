package edu.ucla.wise.persitence.invitee;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.ucla.wise.persistence.invitee.Invitee;
import edu.ucla.wise.persistence.invitee.Invitee.InviteeDAO;
import edu.ucla.wise.persistence.invitee.InviteeStatus;
import edu.ucla.wise.shared.persistence.HibernateConfiguration;
import edu.ucla.wise.shared.persistence.HibernateUtil;
import edu.ucla.wise.shared.properties.DataBaseProperties;

/**
 * TODO: This test is incomplete
 * 
 * @author pdessai
 * 
 */
public class InviteeTest {

    public final List<Class<? extends Object>> ANNOTATED_CLASSES = ImmutableList.of(Invitee.class, InviteeStatus.class);

    @Test
    public void testInviteeRetrieval() {

        DataBaseProperties databaseProperties = new DatabaseProperties();
        HibernateConfiguration hibernateConfig = new HibernateConfiguration(databaseProperties, this.ANNOTATED_CLASSES);
        HibernateUtil hibernateUtil = new HibernateUtil(hibernateConfig);

        hibernateUtil.beginTransaction();

        InviteeDAO inviteeDAO = new InviteeDAO(hibernateUtil);

        Invitee firstInvitee = inviteeDAO.find(4);

        assertEquals(firstInvitee.getFirstName(), "Pralav");

        hibernateUtil.commitTransaction();

    }

    public static class DatabaseProperties implements DataBaseProperties {

        @Override
        public String getJdbcDriver() {
            return "com.mysql.jdbc.Driver";
        }

        @Override
        public String getJdbcUrl() {
            return "jdbc:mysql://localhost:3306/wisedev";
        }

        @Override
        public String getJdbcUsername() {
            return "root";
        }

        @Override
        public String getJdbcPassword() {
            return "";
        }

    }
}
