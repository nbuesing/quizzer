package com.github.nbuesing.quiz.requesthandler.config;

import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] BYPASS_LIST = {
        "/actuator/**",
        "favicon.ico"
    };

    private final ApplicationProperties applicationProperties;

    private final MessageSource messageSource;

    public SecurityConfig(
            final ApplicationProperties applicationProperties,
            final MessageSource messageSource) {
        this.applicationProperties = applicationProperties;
        this.messageSource = messageSource;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .cors()
                .and()
            .authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
            .csrf()
                .disable();
        // @formatter:on
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        final CorsConfiguration configuration = new CorsConfiguration();
        // never do this in production, but for demo purposes.
        configuration.setAllowedOrigins(getOrigins());
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS")
        );
        configuration.setAllowedHeaders(Arrays.asList(
                "access-control-allow-origin",
                "authorization",
                "Content-Type",
                "x-auth-token")
        );
        configuration.setExposedHeaders(Arrays.asList(
                "x-auth-token")
        );

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    private List<String> getOrigins() {

        final List<String> result = new ArrayList<>();

        result.add("http://localhost:8080");
        result.add("http://request-handler:8080");

        result.add("http://nginx:9080");
        result.add("http://localhost:9080");
        result.add("http://rh1:9081");
        result.add("http://rh2:9082");


        result.addAll(applicationProperties.getOrigins());

        try {

            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {

                NetworkInterface iface = interfaces.nextElement();

                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String address = addr.getHostAddress();
                    if (!InetAddressValidator.getInstance().isValidInet4Address(address)) {
                        continue;
                    }
                    result.add("http://" + address + ":8080");
                }
            }
        } catch (final SocketException e) {
            throw new RuntimeException(e);
        }

        log.info("ORIGINS : " + result);

        return result;
    }
}
