package messaging.simpleclient;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.jms.*;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Stateless
public class JmsIntegrationBean {
    private QueueConnectionFactory queueConnectionFactory = null;
    private QueueConnection qConnection = null;
    private QueueSession qSession = null;
    private MessageProducer dlqProducer = null;

    @PostConstruct
    public void init() {
        try {
            InitialContext initialContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory) initialContext.lookup("java:/JmsXA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Queue lookupQueue(String jndiEntry) throws Exception {
        InitialContext initialContext = new InitialContext();
        return (Queue) initialContext.lookup(jndiEntry);
    }

    public int countMessagesInQ(Queue queue) throws Exception {
        int count = 0;
        qConnection = queueConnectionFactory.createQueueConnection();
        qSession = qConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        QueueBrowser qBrowser = qSession.createBrowser(queue);
        qConnection.start();
        Enumeration<?> enumeration = qBrowser.getEnumeration();
        while (enumeration.hasMoreElements()) {
            enumeration.nextElement();
            count++;
        }
        qBrowser.close();
        qSession.close();
        qConnection.close();
        return count;
    }

    public List<Message> getMessagesNoWait(Queue queue) throws Exception {
        QueueConnection eqConnection = queueConnectionFactory.createQueueConnection();
        QueueSession eqSession = eqConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        QueueReceiver eqReceiver = eqSession.createReceiver(queue);
        eqConnection.start();

        List<Message> result = new ArrayList<Message>();

        Message message = null;
        while ((message = eqReceiver.receiveNoWait()) != null) {
            result.add(message);
        }

        eqReceiver.close();
        eqSession.close();
        eqConnection.close();

        return result;
    }

    public void send(String messageBody, Queue queue) throws Exception {
        QueueConnection eqConnection = queueConnectionFactory.createQueueConnection();
        QueueSession eqSession = eqConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        MessageProducer eqProducer = eqSession.createProducer(queue);
        eqConnection.start();

        TextMessage textMessage = eqSession.createTextMessage(messageBody);
        eqProducer.send(textMessage);

        eqProducer.close();
        eqSession.close();
        eqConnection.close();
    }
}
