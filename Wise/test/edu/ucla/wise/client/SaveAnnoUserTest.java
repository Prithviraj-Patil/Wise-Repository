package edu.ucla.wise.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

/**
 * TODO:This test is incomplete.
 * 
 * @author pdessai
 * 
 */
public class SaveAnnoUserTest {

    @Test
    public void testSaveAnnoUserServlet() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn("1234");
        when(request.getParameter("SID")).thenReturn("1234");
        when(request.getParameter("t")).thenReturn("IOOFA");

        new AnonymousUserSaverServlet().doPost(request, response);
    }

}
