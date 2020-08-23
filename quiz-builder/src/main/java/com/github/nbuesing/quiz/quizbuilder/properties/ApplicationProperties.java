/*
 * Copyright (c) 2020.
 */

package com.github.nbuesing.quiz.quizbuilder.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "application")
@Data
@Validated
public class ApplicationProperties {

  @NotNull private String questions;
  @NotNull private String questionsDifficulty;

  @NotNull  private Boolean cleanupOnStart = Boolean.FALSE;

}
