package com.flipkart.gap.usl.container;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ajaysingh on 05/10/16.
 */
@Slf4j
public abstract class USLContainer extends Application<USLBootstrapConfig> {
    private static Random random = new Random();

    @Override
    public void initialize(Bootstrap<USLBootstrapConfig> bootstrap) {
        bootstrap.addBundle(new SwaggerBundle<USLBootstrapConfig>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(USLBootstrapConfig configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    public void run(USLBootstrapConfig uslBootstrapConfig, Environment environment) throws Exception {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        ContextInitializer initializer = new ContextInitializer(context);
        initializer.autoConfig();
        environment.getObjectMapper().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        registerResources(environment);
        addHealthCheck(environment);
        addFilters(environment);
        attachGCHack();
    }

    protected void addHealthCheck(Environment environment) {
        final USLHealthCheck healthCheck = new USLHealthCheck();
        environment.healthChecks().register("healthcheck", healthCheck);
        environment.jersey().register(healthCheck);
    }

    protected abstract void registerResources(Environment environment);

    protected abstract void addFilters(Environment environment) ;


    private void attachGCHack() {
        int startOffset = random.nextInt(1000);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                long startTime = System.currentTimeMillis();
                System.gc();
                log.info("GC ran successfully in {} ms", (System.currentTimeMillis() - startTime));
            } catch (Throwable throwable) {
                log.error("Exception in calling gc ", throwable);
            }
        }, startOffset, 3600, TimeUnit.SECONDS);
        log.info("GC Hack attached with offset {}", startOffset);
    }
}
