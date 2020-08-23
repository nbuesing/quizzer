package com.github.nbuesing.quiz.requesthandler.customerorder.web;

import com.github.nbuesing.quiz.requesthandler.customerorder.service.CustomerProducer;
import com.github.nbuesing.quiz.requesthandler.customerorder.domain.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Typically would be in a separate customer service project. Added to request-handler project to save time during the on-site exercises.
 */
@RestController
@Slf4j
public class CustomerController {

    @Autowired
    private CustomerProducer customerProducer;

    @PostMapping("/customers")
    public Customer save(@RequestBody Customer customer) {
        customerProducer.publish(customer);
        return customer;
    }
}
