package com.github.nbuesing.quiz.requesthandler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "application")
@Data
@Validated
public class ApplicationProperties {

  @NotEmpty private String userTopic;
  @NotEmpty private String questionTopic;
  @NotEmpty private String quizStartTopic;
  @NotEmpty private String quizSubmissionTopic;
  @NotEmpty private String quizNextTopic;
  @NotEmpty private String quizResultTopic;

  @NotNull
  private Duration pollInterval = Duration.ofMillis(200L);

  @NotNull
  private Duration offsetCommitInterval = Duration.ofMillis(10000L);

  @NotNull
  private List<String> origins = new ArrayList<>();
}
