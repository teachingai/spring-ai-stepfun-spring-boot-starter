package org.springframework.ai.stepfun.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(StepFunAiConnectionProperties.CONFIG_PREFIX)
public class StepFunAiConnectionProperties extends StepFunAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.stepfun";

}
