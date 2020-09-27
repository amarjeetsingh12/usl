package com.flipkart.gap.usl.core.store.dimension.resilence;

import com.flipkart.gap.usl.core.config.resilience.ApplicationResilienceConfig;
import com.flipkart.gap.usl.core.config.resilience.ResilienceConfig;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.google.inject.Inject;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.metrics.BulkheadMetrics;
import io.github.resilience4j.metrics.CircuitBreakerMetrics;
import io.github.resilience4j.metrics.RateLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class ResilenceDecorator {
    @Inject
    private ApplicationResilienceConfig applicationResilienceConfig;
    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bulkhead> bulkheadMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final BulkheadRegistry bulkheadRegistry = BulkheadRegistry.ofDefaults();
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    public static final String DIMENSION_READ_SERVICE = "dimensionRead";
    public static final String DIMENSION_BULK_READ_SERVICE = "dimensionBulkRead";
    public static final String DIMENSION_BULK_SAVE_SERVICE = "dimensionBulkSave";
    public static final String DIMENSION_DELETE_SERVICE = "dimensionDelete";
    public static final ResilienceConfig DEFAULT_CONFIG = new ResilienceConfig();

    @Inject
    public void init() {
    }

    private ResilienceConfig getConfig(String serviceName) {
        switch (serviceName) {
            case DIMENSION_READ_SERVICE:
                return applicationResilienceConfig.getDimensionReadConfig();
            case DIMENSION_BULK_READ_SERVICE:
                return applicationResilienceConfig.getDimensionBulkReadConfig();
            case DIMENSION_BULK_SAVE_SERVICE:
                return applicationResilienceConfig.getDimensionBulkSaveConfig();
            case DIMENSION_DELETE_SERVICE:
                return applicationResilienceConfig.getDimensionDeleteConfig();
            default:
                return DEFAULT_CONFIG;
        }
    }

    private CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakerMap.computeIfAbsent(serviceName, s -> {
            com.flipkart.gap.usl.core.config.resilience.CircuitBreakerConfig circuitBreakerConfig = getConfig(serviceName).getCircuitBreakerConfig();
            CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                    .waitDurationInOpenState(Duration.ofMillis(circuitBreakerConfig.getWaitDurationInOpenState()))
                    .minimumNumberOfCalls(circuitBreakerConfig.getMinimumNumberOfCalls())
                    .slidingWindowSize(circuitBreakerConfig.getSlidingWindowSize())
                    .permittedNumberOfCallsInHalfOpenState(circuitBreakerConfig.getPermittedNumberOfCallsInHalfOpenState())
                    .slowCallDurationThreshold(Duration.ofMillis(circuitBreakerConfig.getSlowCallDurationThreshold()))
                    .slowCallRateThreshold(circuitBreakerConfig.getSlowCallRateThreshold())
                    .failureRateThreshold(circuitBreakerConfig.getFailureRateThreshold())
                    .build();
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName, config);
            JmxReporterMetricRegistry.getMetricRegistry().register(serviceName, CircuitBreakerMetrics.ofCircuitBreaker(circuitBreaker));
            return circuitBreaker;
        });
    }

    private RateLimiter getRateLimiter(String serviceName) {
        return rateLimiterMap.computeIfAbsent(serviceName, s -> {
            com.flipkart.gap.usl.core.config.resilience.RateLimiterConfig rateLimiterConfig = getConfig(serviceName).getRateLimiterConfig();
            RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofMillis(rateLimiterConfig.getLimitRefreshPeriod()))
                    .limitForPeriod(rateLimiterConfig.getLimitForPeriod())
                    .timeoutDuration(Duration.ofMillis(rateLimiterConfig.getTimeoutDuration()))
                    .build();
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(serviceName, config);
            JmxReporterMetricRegistry.getMetricRegistry().register(serviceName, RateLimiterMetrics.ofRateLimiter(rateLimiter));
            return rateLimiter;
        });
    }

    private Bulkhead getBulkhead(String serviceName) {
        return bulkheadMap.computeIfAbsent(serviceName, s -> {
            com.flipkart.gap.usl.core.config.resilience.BulkheadConfig bulkheadConfig = getConfig(serviceName).getBulkheadConfig();
            BulkheadConfig config = BulkheadConfig.custom()
                    .maxConcurrentCalls(bulkheadConfig.getMaxConcurrentCalls())
                    .maxWaitDuration(Duration.ofMillis(bulkheadConfig.getMaxWaitDuration()))
                    .build();
            Bulkhead bulkhead = bulkheadRegistry.bulkhead(serviceName, config);
            JmxReporterMetricRegistry.getMetricRegistry().register(serviceName, BulkheadMetrics.ofBulkhead(bulkhead));
            return bulkhead;
        });
    }

    public <T> T execute(String serviceName, Supplier<T> supplier) throws DecoratorExecutionException {
        com.flipkart.gap.usl.core.config.resilience.TimeLimiterConfig timeLimiterConfig = getConfig(serviceName).getTimeLimiterConfig();
        Supplier<T> decorateSupplier = Decorators.ofSupplier(supplier)
                .withCircuitBreaker(getCircuitBreaker(serviceName))
                .withBulkhead(getBulkhead(serviceName))
                .withRateLimiter(getRateLimiter(serviceName))
                .decorate();
        try {
            return CompletableFuture.supplyAsync(() -> Try.ofSupplier(decorateSupplier).get()).get(timeLimiterConfig.getTimeoutDuration(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DecoratorExecutionException(e);
        }
    }
}
