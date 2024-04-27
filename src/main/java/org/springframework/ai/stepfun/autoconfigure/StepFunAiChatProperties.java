package org.springframework.ai.stepfun.autoconfigure;

import org.springframework.ai.stepfun.api.StepFunAiApi;
import org.springframework.ai.stepfun.api.StepFunAiChatOptions;
import org.springframework.ai.stepfun.util.ApiUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(StepFunAiChatProperties.CONFIG_PREFIX)
public class StepFunAiChatProperties extends StepFunAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.stepfun.chat";


    /**
     * Enable stepfun chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever stepFun options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private StepFunAiChatOptions options = StepFunAiChatOptions.builder()
            .withModel(StepFunAiApi.ChatModel.STEP_1V.getValue())
            .withMaxToken(ApiUtils.DEFAULT_MAX_TOKENS)
            .withDoSample(Boolean.TRUE)
            .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
            .withTopP(ApiUtils.DEFAULT_TOP_P)
            .build();

    public StepFunAiChatOptions getOptions() {
        return this.options;
    }

    public void setOptions(StepFunAiChatOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
