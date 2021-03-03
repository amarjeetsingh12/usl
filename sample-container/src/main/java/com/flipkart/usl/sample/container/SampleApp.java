package com.flipkart.usl.sample.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.flipkart.gap.usl.container.USLContainer;
import com.flipkart.gap.usl.container.config.ContainerConfig;

import java.io.File;
import java.util.Arrays;

public class SampleApp {
    private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void main(String[] args) throws Exception {
        ContainerConfig containerConfig = yamlMapper.readValue(new File(args[2]), ContainerConfig.class);
        USLContainer uslContainer = new USLContainer(containerConfig);
        uslContainer.run(Arrays.copyOfRange(args, 0, 2));
    }
}
