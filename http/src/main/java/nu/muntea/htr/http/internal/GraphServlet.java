package nu.muntea.htr.http.internal;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import nu.muntea.htr.storage.api.Storage;

@Component(service = Servlet.class)
@HttpWhiteboardServletPattern("/graph.png")
public class GraphServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    private final Storage storage;

    @Activate
    public GraphServlet(@Reference Storage storage) {
        this.storage = storage;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        resp.setContentType("image/png");
        storage.renderGraph(now().minus(5, MINUTES), now(), resp.getOutputStream());
    }
}
