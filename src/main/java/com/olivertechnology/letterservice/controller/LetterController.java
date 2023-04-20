package com.olivertechnology.letterservice.controller;

import com.olivertechnology.letterservice.Processor;
import com.olivertechnology.letterservice.model.GetMergedData;
import com.olivertechnology.letterservice.model.ProcessDocument;
import com.olivertechnology.letterservice.model.MailDocument;
import com.olivertechnology.letterservice.model.MailDocumentDelayed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LetterController {

  @Autowired
  private Processor processor;


  @PostMapping("/letterservice/getmergeddata")
  String doGetMergedData(@RequestBody GetMergedData request) {

    String result = null;

    try {
      result = processor.buildGetMergedDataRequest(request);
    } catch (Exception ex) {
      log.error("doGetMergedData error: ", ex);
    }
    log.info(result);
    return result;
  }

  @PostMapping("/letterservice/processdocument")
  String doProcessDocument(@RequestBody ProcessDocument request) {

    String result = null;

    try {
      result = processor.buildProcessDocumentRequest(request);
    } catch (Exception ex) {
      log.error("doProcessDocument error: ", ex);
    }
    log.info(result);
    return "{ \"message\": \"" + result + "\" }";
  }

  @PostMapping("/letterservice/maildocument")
  String doMailDocument(@RequestBody MailDocument request) {

    String result = null;

    try {
      result = processor.buildMailDocumentRequest(request);
    } catch (Exception ex) {
      log.error("doMailDocument error: ", ex);
    }
    log.info(result);
    return "{ \"message\": \"" + result + "\" }";
  }

  @PostMapping("/letterservice/maildocumentdelayed")
  String doMailDocumentDelayed(@RequestBody MailDocumentDelayed request) {

    String result = null;

    try {
      result = processor.buildMailDocumentDelayedRequest(request);
    } catch (Exception ex) {
      log.error("doMailDocumentDelayed error: ", ex);
    }
    log.info(result);
    return "{ \"message\": \"" + result + "\" }";
  }
}

