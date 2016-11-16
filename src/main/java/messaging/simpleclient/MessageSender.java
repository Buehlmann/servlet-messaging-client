package messaging.simpleclient;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.naming.NameNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/send")
@SuppressWarnings("serial")
public class MessageSender extends HttpServlet {
    @Inject
    private JmsIntegrationBean messagingClient;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
            out.append("Served at: ").append(request.getContextPath()).append("\r\n");
            String jndiEntry = request.getParameter("queue");
            System.out.println("Resolved parameter 'queue' to: " + jndiEntry);
            if (jndiEntry == null || jndiEntry.isEmpty()) {
                return;
            }
            System.out.println("Looking up: " + jndiEntry);

            try {
                Queue queue = messagingClient.lookupQueue(jndiEntry);
                System.out.println("Successfully looked up " + jndiEntry + " on message broker");

                messagingClient.send("Hello from messaging client!", queue);

                String s = "Successfully sent message to queue " + jndiEntry;
                System.out.println(s);
                out.append(s);

            } catch (NameNotFoundException e) {
                out.append("Queue was not found in JNDI of message broker: " + jndiEntry);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}
