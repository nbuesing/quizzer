package com.github.nbuesing.quiz.requesthandler.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseChild {

    /**
     * The original event
     */
    private Request event;

    private String instance;

    private String status;

}
