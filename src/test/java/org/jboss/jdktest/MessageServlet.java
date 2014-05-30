package org.jboss.jdktest;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MessageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {
        int length = req.getContentLength();
        if (length > 0) {
            byte[] buffer = new byte[length];
            req.getInputStream().read(buffer);
        }
        PrintWriter writer = resp.getWriter();
        writer
            .write("helloworld!helloworld!helloworld!helloworld!helloworld!helloworld!helloworld!helloworld!helloworld!helloworld!");
        writer.close();
    }
}
