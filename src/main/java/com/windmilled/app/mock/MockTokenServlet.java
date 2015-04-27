package com.windmilled.app.mock;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Mock servlet for user token validation, only for testing purposes.
 * '11111' - valid token, others are invalid
 */
public class MockTokenServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String token = request.getHeader("Token");
        PrintWriter out = response.getWriter();
        if ((token != null) && token.equals("11111")) {
            out.println("{\"response\": {\"status\":\"Success\"}}");
        } else {
            out.println("{\"response\": {\"status\":\"Fail\"}}");
        }
    }
}
