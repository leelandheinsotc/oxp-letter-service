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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class ProcessDocument {

  public String matterUuid;
  public String templateReference;

  static String documentManagementUrl;

  @Value("${DM.documentManagementUrl}")
  public void setDocumentManagementUrl(String documentManagementUrl) { ProcessDocument.documentManagementUrl = documentManagementUrl; }

  public static String processDocument(String rMatterUuid, String rTemplateReference) {
    log.info("Process Document");
    log.info("rMatterUuid=" + rMatterUuid);
    log.info("rTemplateReference=" + rTemplateReference);

    String result = "Processing success";

    String mergedData = "";

    // Call GetMergedData
    try {
      mergedData = GetMergedData.getMergedData(rMatterUuid, rTemplateReference);
      log.info("1 mergedData=" + mergedData);
      //mergedData = mergedData.replaceAll("\\\\\"", "\"");
      //log.info("2 mergedData=" + mergedData);
    } catch (Exception e) {
      log.error("GetMergedData failure " + e);
      result = "GetMergedData failure";
    }

    // Follow naming convention here.  We add the date and time and change .docx to .pdf
    DateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmssSS");
    Calendar cal = Calendar.getInstance();
    String timeStamp = df.format(cal.getTime());

    String outputPdf = timeStamp + "_" + rTemplateReference.replace(".docx", ".pdf");
    log.info("outputPdf=" + outputPdf);

    // Call Docx2Pdf from document management service
    String jsonBody = "{ \"templateDocx\" : \"" + rTemplateReference + "\", "
            + " \"mergedData\" : \"" + mergedData + "\", "
            + " \"outputPdf\" : \"" + outputPdf + "\" }";
    log.info("jsonBody=" + jsonBody);

    log.info("calling " + ProcessDocument.documentManagementUrl + "docx2pdf");

    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      HttpEntity postData = EntityBuilder.create()
              .setContentType(ContentType.parse("application/json"))
              .setText(jsonBody)
              .build();
      HttpUriRequest req = RequestBuilder
              .post(ProcessDocument.documentManagementUrl + "docx2pdf")
              .setEntity(postData)
              .build();
      CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(req);
      log.info("response=" + response);
      //FIXME -- need to check for failure
    } catch (Exception ex) {
      log.error("Document Management failure - " + ex);
      result = "Document Management failure";
    } finally {
      try {
        httpClient.close();
      } catch (IOException ex) {
        log.error("Document Management failure - " + ex);
        result = "Document Management failure";
      }
    }

    //FIXME -- need to figure out how to get this stuff
    String approvedBy = "oxp";
    String tags = "";
    String description = "";
    String fileNo = "";

    // Call MoveTempToDocuments from document management service
    jsonBody = "{ \"outputPdf\" : \"" + outputPdf + "\", "
            + " \"approvedBy\" : \"" + approvedBy + "\", "
            + " \"tags\" : \"" + tags + "\", "
            + " \"matterUuid\" : \"" + rMatterUuid + "\", "
            + " \"description\" : \"" + description + "\", "
            + " \"fileNo\" : \"" + fileNo + "\" }";
    log.info("jsonBody=" + jsonBody);

    httpClient = HttpClients.createDefault();
    try {
      HttpEntity postData = EntityBuilder.create()
              .setContentType(ContentType.parse("application/json"))
              .setText(jsonBody)
              .build();
      HttpUriRequest req = RequestBuilder
              .post(ProcessDocument.documentManagementUrl + "movetemptodocuments")
              .setEntity(postData)
              .build();
      CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(req);
      log.info("response=" + response);
      //FIXME -- need to check for failure
    } catch (Exception ex) {
      log.error("Document Management failure - " + ex);
      result = "Document Management failure";
    } finally {
      try {
        httpClient.close();
      } catch (IOException ex) {
        log.error("Document Management failure - " + ex);
        result = "Document Management failure";
      }
    }

    return result;
  }
}

