package com.github.nbuesing.quiz.requesthandler.customerorder.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Typically would be in a separate customer service project. Added to request-handler project to save time during the on-site exercises.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Customer {

    public String id;
    public String name;

}
