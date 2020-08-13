package com.flipkart.gap.usl.core.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ServiceClientConfig {
    @NotNull
    @NotEmpty
    private String host;
    private int port;
    @Min(2)
    private short poolSize = 5;
    @Min(200)
    private short connectionTimeout = 1000;
    @Min(200)
    private short requestTimeout = 2000;
    @Min(500)
    private int maxQueueSize = 2000;
    @Min(200)
    private int batchSize = 1000;
}
