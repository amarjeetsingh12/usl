package com.flipkart.gap.usl.container.guiceconfig;

import com.flipkart.gap.usl.container.config.ClientContainerConfig;
import com.google.inject.AbstractModule;
import lombok.Getter;

@Getter
public abstract class ClientConfigModule<T extends ClientContainerConfig> extends AbstractModule {
    protected T configuration;

    public ClientConfigModule(T configuration) {
        this.configuration = configuration;
    }
}
