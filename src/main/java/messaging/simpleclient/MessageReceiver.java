package messaging.simpleclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.naming.NameNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/receive")
@SuppressWarnings("serial")
public class MessageReceiver extends HttpServlet {
    @Inject
    private JmsIntegrationBean messagingClient;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
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
                out.append("# of messages in " + jndiEntry + ": ").append(messagingClient.countMessagesInQ(queue) + "\r\n\r\n");

                for (Message message : messagingClient.getMessagesNoWait(queue)) {
                    out.append(message.toString()).append(" - " + ((TextMessage) message).getText() + "\r\n");

                    Enumeration<?> keys = message.getPropertyNames();
                    while (keys.hasMoreElements()) {
                        Object key = keys.nextElement();
                        out.append(key + "").append(": ").append(message.getObjectProperty(key.toString()) + "\r\n");
                    }
                    out.append("\r\n");
                }
            } catch (NameNotFoundException e) {
                System.out.println("Queue was not found in JNDI of message broker: " + jndiEntry);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
