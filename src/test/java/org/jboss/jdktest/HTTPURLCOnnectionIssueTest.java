package org.jboss.jdktest;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HTTPURLCOnnectionIssueTest {
    long getOutpuStreamTime = 0L;
    List<Long> resultList = new ArrayList<Long>();
    static Undertow server = null;

    @BeforeClass
    public static void startUndertowServer() {
        final String MYAPP = "/jdktest";
        DeploymentInfo servletBuilder = deployment().setClassLoader(MessageServlet.class.getClassLoader())
            .setContextPath(MYAPP).setDeploymentName("test.war")
            .addServlets(servlet("MessageServlet", MessageServlet.class).addMapping("/*"));

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        HttpHandler servletHandler = null;
        try {
            servletHandler = manager.start();
        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PathHandler path = Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP, servletHandler);
        server = Undertow.builder().addHttpListener(8000, "localhost").setHandler(path).build();
        server.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testGetoutputStreamWithFixedLength() throws Exception {
        testFixedLengthMode(true);
    }

    @Test
    public void testGetoutputStream() throws Exception {
        testFixedLengthMode(false);
    }

    public void testFixedLengthMode(boolean fixedLengthMode) throws Exception {
        getOutpuStreamTime = 0L;
        resultList.clear();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("-------------------------------------------------------------------");
        System.out
            .println("***** Test with java version : " + System.getProperty("java.version") + " ***** ");
        if (fixedLengthMode) {
            System.out.println("!!! connection.setFixedLengthStreamingMode() has been set !!!");
        } else {
            System.out.println("!!! connection.setFixedLengthStreamingMode()  is  NOT set !!!");
        }
        URL url = new URL("http://localhost:8000/jdktest/");
        int WARMNUM = 100;
        for (int i = 0; i < WARMNUM; i++) {
            callServer(url, true, fixedLengthMode);
        }

        int CALLNUM = 300;
        int SUCCESSNUM = 0;
        long begin = System.currentTimeMillis();
        for (int i = 0; i < CALLNUM; i++) {
            if (callServer(url, false, fixedLengthMode)) {
                SUCCESSNUM++;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("CallNum: [" + CALLNUM + "] SuccessNUM: [" + SUCCESSNUM + "] Time: ["
                           + (end - begin) + "]  Total getOutputStream Time : ["
                           + Long.toOctalString(getOutpuStreamTime) + "]");
        System.out.println("Each getOutputStream time:");
        System.out.println(resultList);
    }

    public boolean callServer(URL url, boolean warmUp, boolean fixedLength) throws Exception {
        String requestBody = "hello world request";
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        // this setting will affect the getOutputStream after jdk7U40
        if (fixedLength) {
            connection.setFixedLengthStreamingMode(requestBody.length());
        }
        connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        connection.setRequestProperty("Accept", "*/*");

        long start = System.currentTimeMillis();
        OutputStream outputStream = connection.getOutputStream();
        long end = System.currentTimeMillis();

        outputStream.write(requestBody.getBytes());
        if (!warmUp)
            resultList.add(end - start);
        outputStream.flush();

        if (!warmUp)
            getOutpuStreamTime = getOutpuStreamTime + (end - start);
        int responseCode = connection.getResponseCode();
        byte buffer[] = new byte[connection.getContentLength()];
        connection.getInputStream().read(buffer);
        connection.getInputStream().close();
        outputStream.close();
        String result = new String(buffer);
        if (responseCode == 200 && result.contains("helloworld!")) {
            return true;
        }
        return false;
    }

}
