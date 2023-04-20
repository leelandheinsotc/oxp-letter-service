package com.olivertechnology.letterservice.model;

import com.olivertechnology.letterservice.util.SQSUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Data
@Slf4j
public class MailDocumentDelayed {

  public String matterUuid;
  public String templateReference;
  static String queueEndPoint;
  static String queueName;

  public static String mailDocumentDelayed(String rMatterUuid, String rTemplateReference) {
    log.info("Mail Document Delayed");

    String result = "Mailing Scheduling success";

    // Add a message to the delay SQS here
    String jsonString = "{ \"matterUuid\" : \"" + rMatterUuid + "\", \"templateReference\" : \"" + rTemplateReference + "\" }";
    log.info("jsonString=" + jsonString);

    SQSUtils sqs = new SQSUtils();
    String pushRes = sqs.pushToDelayQueue(queueEndPoint + queueName, jsonString);
    log.info("pushRes=" + pushRes);
    if (pushRes.contains("failure")) {
      result = "SQS push failure";
    }

    return result;
  }

  @Value("${SQS.queueEndPoint}")
  public void setQueueEndPoint(String queueEndPoint) {
    MailDocumentDelayed.queueEndPoint = queueEndPoint;
  }
  @Value("${SQS.queueName}")
  public void setQueueUrl(String queueName) {
    MailDocumentDelayed.queueName = queueName;
  }
}

