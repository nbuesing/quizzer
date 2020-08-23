package com.github.nbuesing.quiz.requesthandler.customerorder.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Typically would be in a separate order service project. Added to request-handler project to save time during the on-site exercises.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order {

    private String id;
    private String customerId;
    private BigDecimal amount;

}
