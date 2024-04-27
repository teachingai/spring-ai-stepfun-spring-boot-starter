package org.springframework.ai.stepfun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.stepfun.api.StepFunAiApi;
import org.springframework.ai.stepfun.api.StepFunAiChatOptions;
import org.springframework.ai.stepfun.util.ApiUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StepFunAiChatClient
        extends AbstractFunctionCallSupport<StepFunAiApi.ChatCompletionMessage, StepFunAiApi.ChatCompletionRequest, ResponseEntity<StepFunAiApi.ChatCompletion>>
        implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private StepFunAiChatOptions defaultOptions;
    /**
     * Low-level stepFun API library.
     */
    private final StepFunAiApi stepFunAiApi;
    private final RetryTemplate retryTemplate;

    public StepFunAiChatClient(StepFunAiApi stepFunAiApi) {
        this(stepFunAiApi, StepFunAiChatOptions.builder()
                        .withModel(StepFunAiApi.ChatModel.STEP_1V.getValue())
                        .withMaxToken(ApiUtils.DEFAULT_MAX_TOKENS)
                        .withDoSample(Boolean.TRUE)
                        .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                        .withTopP(ApiUtils.DEFAULT_TOP_P)
                        .build());
    }

    public StepFunAiChatClient(StepFunAiApi stepFunAiApi, StepFunAiChatOptions options) {
        this(stepFunAiApi, options, null, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public StepFunAiChatClient(StepFunAiApi stepFunAiApi, StepFunAiChatOptions options,
                               FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate) {
        super(functionCallbackContext);
        Assert.notNull(stepFunAiApi, "StepFunAiApi must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.stepFunAiApi = stepFunAiApi;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }


    @Override
    public ChatResponse call(Prompt prompt) {

        var request = createRequest(prompt, false);

        return retryTemplate.execute(ctx -> {

            ResponseEntity<StepFunAiApi.ChatCompletion> completionEntity = this.callWithFunctionSupport(request);

            var chatCompletion = completionEntity.getBody();
            if (chatCompletion == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }

            List<Generation> generations = chatCompletion.choices()
                    .stream()
                    .map(choice -> new Generation(choice.message().content(), toMap(chatCompletion.id(), choice))
                            .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null)))
                    .toList();

            return new ChatResponse(generations);
        });
    }

    private Map<String, Object> toMap(String id, StepFunAiApi.ChatCompletion.Choice choice) {
        Map<String, Object> map = new HashMap<>();

        var message = choice.message();
        if (message.role() != null) {
            map.put("role", message.role().name());
        }
        if (choice.finishReason() != null) {
            map.put("finishReason", choice.finishReason().name());
        }
        map.put("id", id);
        return map;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        var request = createRequest(prompt, true);

        return retryTemplate.execute(ctx -> {

            var completionChunks = this.stepFunAiApi.chatCompletionStream(request);

            // For chunked responses, only the first chunk contains the choice role.
            // The rest of the chunks with same ID share the same role.
            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            return completionChunks.map(this::toChatCompletion).map(chatCompletion -> {

                chatCompletion = handleFunctionCallOrReturn(request, ResponseEntity.of(Optional.of(chatCompletion)))
                        .getBody();

                @SuppressWarnings("null")
                String id = chatCompletion.id();

                List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                    if (choice.message().role() != null) {
                        roleMap.putIfAbsent(id, choice.message().role().name());
                    }
                    String finish = (choice.finishReason() != null ? choice.finishReason().name() : "");
                    var generation = new Generation(choice.message().content(),
                            Map.of("id", id, "role", roleMap.get(id), "finishReason", finish));
                    if (choice.finishReason() != null) {
                        generation = generation
                                .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
                    }
                    return generation;
                }).toList();
                return new ChatResponse(generations);
            });
        });
    }

    private StepFunAiApi.ChatCompletion toChatCompletion(StepFunAiApi.ChatCompletionChunk chunk) {
        List<StepFunAiApi.ChatCompletion.Choice> choices = chunk.choices()
                .stream()
                .map(cc -> new StepFunAiApi.ChatCompletion.Choice(cc.index(), cc.delta(), cc.finishReason()))
                .toList();

        return new StepFunAiApi.ChatCompletion(chunk.id(), "chat.completion", chunk.created(), chunk.model(), choices, chunk.requestId(),null);
    }

    /**
     * Accessible for testing.
     */
    StepFunAiApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

        Set<String> functionsForThisRequest = new HashSet<>();

        var chatCompletionMessages = prompt.getInstructions()
                .stream()
                .map(m -> new StepFunAiApi.ChatCompletionMessage(m.getContent(),
                        StepFunAiApi.ChatCompletionMessage.Role.valueOf(m.getMessageType().name())))
                .toList();

        var request = new StepFunAiApi.ChatCompletionRequest(null, chatCompletionMessages, stream);

        if (this.defaultOptions != null) {
            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);

            request = ModelOptionsUtils.merge(request, this.defaultOptions, StepFunAiApi.ChatCompletionRequest.class);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        StepFunAiChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                        IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request,
                        StepFunAiApi.ChatCompletionRequest.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge(
                    StepFunAiChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, StepFunAiApi.ChatCompletionRequest.class);
        }

        return request;
    }

    private List<StepFunAiApi.FunctionTool> getFunctionTools(Set<String> functionNames) {
        return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = new StepFunAiApi.FunctionTool.Function(functionCallback.getDescription(),
                    functionCallback.getName(), functionCallback.getInputTypeSchema());
            return new StepFunAiApi.FunctionTool(function);
        }).toList();
    }

    //
    // Function Calling Support
    //
    @Override
    protected StepFunAiApi.ChatCompletionRequest doCreateToolResponseRequest(StepFunAiApi.ChatCompletionRequest previousRequest,
                                                                             StepFunAiApi.ChatCompletionMessage responseMessage,
                                                                             List<StepFunAiApi.ChatCompletionMessage> conversationHistory) {

        // Every tool-call item requires a separate function call and a response (TOOL)
        // message.
        for (StepFunAiApi.ChatCompletionMessage.ToolCall toolCall : responseMessage.toolCalls()) {

            var functionName = toolCall.function().name();
            String functionArguments = toolCall.function().arguments();

            if (!this.functionCallbackRegister.containsKey(functionName)) {
                throw new IllegalStateException("No function callback found for function name: " + functionName);
            }

            String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

            // Add the function response to the conversation.
            conversationHistory
                    .add(new StepFunAiApi.ChatCompletionMessage(functionResponse, StepFunAiApi.ChatCompletionMessage.Role.TOOL, functionName, null));
        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        StepFunAiApi.ChatCompletionRequest newRequest = new StepFunAiApi.ChatCompletionRequest(previousRequest.requestId(), conversationHistory, false);
        newRequest = ModelOptionsUtils.merge(newRequest, previousRequest, StepFunAiApi.ChatCompletionRequest.class);

        return newRequest;
    }

    @Override
    protected List<StepFunAiApi.ChatCompletionMessage> doGetUserMessages(StepFunAiApi.ChatCompletionRequest request) {
        return request.messages();
    }

    @SuppressWarnings("null")
    @Override
    protected StepFunAiApi.ChatCompletionMessage doGetToolResponseMessage(ResponseEntity<StepFunAiApi.ChatCompletion> chatCompletion) {
        return chatCompletion.getBody().choices().iterator().next().message();
    }

    @Override
    protected ResponseEntity<StepFunAiApi.ChatCompletion> doChatCompletion(StepFunAiApi.ChatCompletionRequest request) {
        return this.stepFunAiApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<StepFunAiApi.ChatCompletion> chatCompletion) {

        var body = chatCompletion.getBody();
        if (body == null) {
            return false;
        }

        var choices = body.choices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        return !CollectionUtils.isEmpty(choices.get(0).message().toolCalls());
    }
}
