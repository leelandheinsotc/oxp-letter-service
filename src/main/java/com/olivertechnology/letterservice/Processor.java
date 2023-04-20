package com.olivertechnology.letterservice;

import com.olivertechnology.letterservice.model.GetMergedData;
import com.olivertechnology.letterservice.model.ProcessDocument;
import com.olivertechnology.letterservice.model.MailDocument;
import com.olivertechnology.letterservice.model.MailDocumentDelayed;
import org.springframework.stereotype.Service;

@Service
public class Processor {

  public String buildGetMergedDataRequest(GetMergedData getMergedData) {

    return GetMergedData.getMergedData(getMergedData.matterUuid, getMergedData.template);
  }

  public String buildProcessDocumentRequest(ProcessDocument processDocument) {

    return ProcessDocument.processDocument(processDocument.matterUuid, processDocument.templateReference);
  }

  public String buildMailDocumentRequest(MailDocument mailDocument) {

    return MailDocument.mailDocument(mailDocument.matterUuid, mailDocument.templateReference);
  }

  public String buildMailDocumentDelayedRequest(MailDocumentDelayed mailDocumentDelayed) {

    return MailDocumentDelayed.mailDocumentDelayed(mailDocumentDelayed.matterUuid, mailDocumentDelayed.templateReference);
  }
}

