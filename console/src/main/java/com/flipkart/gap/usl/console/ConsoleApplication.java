package com.flipkart.gap.usl.console;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flipkart.gap.usl.console.config.ApplicationConfiguration;
import com.flipkart.gap.usl.console.config.ConfigurationModule;
import com.flipkart.gap.usl.console.resource.EventMappingResource;
import com.flipkart.gap.usl.console.resource.EventTypeResource;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;


/**
 * Created by amarjeet.singh on 22/02/16.
 */

@Slf4j
public class ConsoleApplication extends Application<ApplicationConfiguration> {

    private static Injector injector;

    public static void main(String args[]) throws Exception {
        new ConsoleApplication().run(args);
    }

    private static void setInjector(ApplicationConfiguration applicationConfiguration) {
        injector = Guice.createInjector(new ConfigurationModule(applicationConfiguration));
    }

    @Override
    public void run(ApplicationConfiguration configuration, Environment environment) throws Exception {
        setInjector(configuration);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        ContextInitializer initializer = new ContextInitializer(context);
        initializer.autoConfig();
        //TODO: Initialise DB connections
        environment.getObjectMapper().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        environment.jersey().register(injector.getInstance(EventTypeResource.class));
        environment.jersey().register(injector.getInstance(EventMappingResource.class));
    }
}
