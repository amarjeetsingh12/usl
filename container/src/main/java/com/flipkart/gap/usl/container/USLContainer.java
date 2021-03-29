package com.flipkart.gap.usl.container;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flipkart.gap.usl.container.config.ContainerConfig;
import com.flipkart.gap.usl.container.config.ContainerConfigurationModule;
import com.flipkart.gap.usl.container.config.USLBootstrapConfig;
import com.flipkart.gap.usl.container.filters.RequestFilter;
import com.flipkart.gap.usl.container.resource.*;
import com.flipkart.gap.usl.core.config.ConfigurationModule;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

/**
 * Created by ajaysingh on 05/10/16.
 */
@Slf4j
public class USLContainer extends Application<USLBootstrapConfig> {
    @Getter
    private final ContainerConfig containerConfig;

    public USLContainer(ContainerConfig containerConfig) {
        this.containerConfig = containerConfig;
    }

    @Override
    public void initialize(Bootstrap<USLBootstrapConfig> bootstrap) {
        bootstrap.addBundle(new SwaggerBundle<USLBootstrapConfig>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(USLBootstrapConfig configuration) {
                return containerConfig.getSwaggerBundleConfiguration();
            }
        });
    }

    @Override
    public void run(USLBootstrapConfig uslBootstrapConfig, Environment environment) throws Exception {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        ContextInitializer initializer = new ContextInitializer(context);
        initializer.autoConfig();
        Injector injector = Guice.createInjector(new ConfigurationModule(containerConfig.getCoreConfig()),
                new ContainerConfigurationModule(containerConfig));
        environment.getObjectMapper().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JmxReporterMetricRegistry.initialiseJmxMetricRegistry();
        environment.jersey().register(injector.getInstance(DimensionDeletionResource.class));
        environment.jersey().register(injector.getInstance(EventIngestorResource.class));
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(RetrievalResource.class));
        environment.jersey().register(injector.getInstance(RequestFilter.class));
        environment.jersey().register(injector.getInstance(EventResource.class));
        environment.jersey().register(injector.getInstance(EventMappingResource.class));
        addHealthCheck(environment);
    }

    protected void addHealthCheck(Environment environment) {
        final USLHealthCheck healthCheck = new USLHealthCheck();
        environment.healthChecks().register("healthcheck", healthCheck);
        environment.jersey().register(healthCheck);
    }

}
