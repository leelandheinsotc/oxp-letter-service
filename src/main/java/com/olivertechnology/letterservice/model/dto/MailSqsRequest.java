package com.olivertechnology.letterservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//import javax.validation.Valid;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Valid
public class MailSqsRequest {
    private String matterUuid;
    private String templateReference;
}
