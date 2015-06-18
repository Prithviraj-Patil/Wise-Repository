package edu.ucla.wise.studyspacewizard.authentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

@WebFilter("/LoginPage.jsp")
public class AuthenticatorFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticatorFilter.class);

    @Override
    public void destroy() {
        // do nothing

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(true);
        String authenticated = (String) session.getAttribute("authenticated");
        if (Strings.isNullOrEmpty(authenticated)) {
            if (!"true".equals(authenticated)) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendRedirect("login.html");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Initializing authentication filter");
    }
}
