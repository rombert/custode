package nu.muntea.htr.http.internal;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

@Component(service = Servlet.class)
@HttpWhiteboardServletPattern("/record")
public class RecordStorageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String source = req.getParameter("source");
        if ( source == null || source.length() == 0 ) {
            resp.setStatus(SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            resp.getWriter().write("Missing required paramter 'source'");
            return;
        }
        long temp;
        try {
            temp = Long.parseLong(req.getParameter("temp_celsius"));
        } catch (NumberFormatException e) {
            resp.setStatus(SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            resp.getWriter().write("Missing or invalid paramter 'temp_celsius'");
            return;

        }
        long timestamp;
        try {
            timestamp = Long.parseLong(req.getParameter("timestamp"));
        } catch (NumberFormatException e) {
            resp.setStatus(SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            resp.getWriter().write("Missing or invalid paramter 'timestamp'");
            return;

        }
        if ( timestamp < 0 )
            timestamp = System.currentTimeMillis();
        
        System.out.format("Received data s: %s, t: %d, d: %d%n", source, temp, timestamp);
        
        resp.setStatus(SC_NO_CONTENT);
    }
}
