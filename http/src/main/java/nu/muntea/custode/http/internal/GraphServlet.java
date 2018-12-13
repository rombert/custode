package nu.muntea.custode.http.internal;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import nu.muntea.custode.storage.api.Storage;
import nu.muntea.custode.storage.api.StorageSelector;

@Component(service = Servlet.class)
@HttpWhiteboardServletPattern("/graph.png")
public class GraphServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    private final StorageSelector selector;

    @Activate
    public GraphServlet(@Reference StorageSelector selector) {
        this.selector = selector;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        String a = null;
        resp.getWriter().write(a.toLowerCase());
        
        String minutes = Optional
            .ofNullable(req.getParameter("minutesAgo"))
            .orElse("5");
        
        String source = req.getParameter("source");
        if ( source == null || source.isEmpty() ) {
            resp.setStatus(SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            resp.getWriter().write("Required parameter 'source' not found");
            return;
        }
        
        Optional<Storage> storage = selector.select(source);
        if ( !storage.isPresent() ) {
            resp.setStatus(SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            resp.getWriter().write("No storage found matching requested source");
            return;
        }
        
        try {
            long minutesAgo = Long.parseLong(minutes);
            resp.setContentType("image/png");
            storage.get().renderGraph(now().minus(minutesAgo, MINUTES), now(), resp.getOutputStream());
        } catch ( NumberFormatException e ) {
            resp.setStatus(SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            resp.getWriter().write("Invalid numeric value " + minutes);
        }
    }
}
