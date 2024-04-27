package org.springframework.ai.stepfun.autoconfigure;

import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.stepfun.StepFunAiChatClient;
import org.springframework.ai.stepfun.api.StepFunAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for stepFun Chat Client.
 */
@AutoConfiguration(after = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class})
@EnableConfigurationProperties({StepFunAiChatProperties.class, StepFunAiConnectionProperties.class})
@ConditionalOnClass(StepFunAiApi.class)
public class StepFunAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = StepFunAiChatProperties.CONFIG_PREFIX, name = "enabled")
    public StepFunAiChatClient stepFunAiChatClient(StepFunAiConnectionProperties connectionProperties,
                                                   StepFunAiChatProperties chatProperties,
                                                   List<FunctionCallback> toolFunctionCallbacks,
                                                   FunctionCallbackContext functionCallbackContext,
                                                   RestClient.Builder restClientBuilder,
                                                   ResponseErrorHandler responseErrorHandler,
                                                   ObjectProvider<RetryTemplate> retryTemplateProvider) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }

        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : connectionProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : connectionProperties.getApiKey();
        Assert.hasText(baseUrl, "stepFun AI base URL must be set");
        Assert.hasText(apiKey, "stepFun API key must be set");

        StepFunAiApi stepFunAiApi = new StepFunAiApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);

        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new StepFunAiChatClient(stepFunAiApi, chatProperties.getOptions(), functionCallbackContext, retryTemplate);
    }


    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
