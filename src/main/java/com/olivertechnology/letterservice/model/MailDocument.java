package com.olivertechnology.letterservice.model;

import com.olivertechnology.letterservice.util.DBUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Data
@Slf4j
public class MailDocument {

  public String matterUuid;
  public String templateReference;
  static String lobIntegrationUrl;

  @Value("${LOB.lobIntegrationUrl}")
  public void setLobIntegrationUrl(String lobIntegrationUrl) { MailDocument.lobIntegrationUrl = lobIntegrationUrl; }

  public static String mailDocument(String rMatterUuid, String rTemplateReference) {
    log.info("Mail Document");
    log.info("rMatterUuid=" + rMatterUuid);
    log.info("rTemplateReference=" + rTemplateReference);

    String result = "Mailing success";

    DBUtils db = new DBUtils();

    // Process the document.
    String procResult = ProcessDocument.processDocument(rMatterUuid, rTemplateReference);
    log.info("procResult=" + procResult);
    if (procResult.contains("failure")) {
      result = "processDocument failure";
    }

    // Get the file uuid of the document we just processed.
    String fileUuid = db.getFileUuid(rMatterUuid, rTemplateReference);
    log.info("fileUuid=" + fileUuid);
    if (fileUuid == null || fileUuid.isEmpty() || fileUuid.isBlank()) {
      result = "getFileUuid failure";
    }

    // Call LOB integration to send filled letter
    //FIXME -- this needs to be configurable to allow other mailing vendor integrations
    String jsonBody = "{ \"fileUuid\" : \"" + fileUuid + "\" }";
    log.info("jsonBody=" + jsonBody);

    log.info("calling lobIntegrationUrl " + lobIntegrationUrl);

    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      HttpEntity postData = EntityBuilder.create()
              .setContentType(ContentType.parse("application/json"))
              .setText(jsonBody)
              .build();
      HttpUriRequest req = RequestBuilder
              .post(lobIntegrationUrl)
              .setEntity(postData)
              .build();
      CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(req);
      log.info("response=" + response);
      //FIXME -- need to check for failure
    } catch (Exception ex) {
      log.error("LOB Integration failure - " + ex);
      result = "LOB Integration failure";
    } finally {
      try {
        httpClient.close();
      } catch (IOException ex) {
        log.error("LOB Integration failure - " + ex);
        result = "LOB Integration failure";
      }
    }

    return result;
  }
}

