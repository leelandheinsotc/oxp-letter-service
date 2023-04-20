package com.olivertechnology.letterservice.service;

import com.olivertechnology.letterservice.messaging.JmsPublisher;
import com.olivertechnology.letterservice.util.SerializationUtils;
import com.olivertechnology.letterservice.model.dto.MailSqsRequest;
import com.olivertechnology.letterservice.model.MailDocument;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

import com.google.gson.JsonSyntaxException;

@Service
@Slf4j
@Component
public class MailListenerService {
  private JmsPublisher jmsPublisher;

  private String inBoundQueueName;

  @Autowired
  public MailListenerService(final JmsPublisher jmsPublisher,
                                  @Value("${SQS.queueName}") final String inBoundQueueName) {
    this.jmsPublisher = jmsPublisher;
    this.inBoundQueueName = inBoundQueueName;
  }

  @JmsListener(destination = "${SQS.queueName}")
  public void processMailEventMessages(final String sqsMessage) throws JMSException {
    log.info("messages from SQS {} ", sqsMessage);
    try {
      final MailSqsRequest mailSqsRequest = SerializationUtils.deserialize(sqsMessage, MailSqsRequest.class);

      // Process message
      String matterUuid = mailSqsRequest.getMatterUuid();
      log.info("matterUuid=" + matterUuid);
      String templateReference = mailSqsRequest.getTemplateReference();
      log.info("templateReference=" + templateReference);

      // Call api to mail document
      String mailRes = MailDocument.mailDocument(matterUuid, templateReference);
      log.info("mailRes=" + mailRes);
      if (mailRes.contains("failure")) {
        log.error("Mail failure");
      }
    } catch (final Exception e) {
      log.error("deserialization failed for Mail {} with error: {}", sqsMessage, e.getLocalizedMessage(), e);
      if (e instanceof JsonSyntaxException) {
        log.error("Invalid json " + sqsMessage);
      } else {
        log.error("re-posting message back to queue {} due to internal processing error", inBoundQueueName);
        throw new JMSException("Encounter error while processing the message.");
      }
    }
  }
}

