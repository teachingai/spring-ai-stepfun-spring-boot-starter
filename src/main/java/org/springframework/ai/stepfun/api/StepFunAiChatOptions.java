package org.springframework.ai.stepfun.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StepFunAiChatOptions implements FunctionCallingOptions, ChatOptions {

    /**
     * 模型输出最大 tokens，最大输出为8192，默认值为1024
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    /**
     * do_sample 为 true 时启用采样策略，do_sample 为 false 时采样策略 temperature、top_p 将不生效。默认值为 true。
     */
    @JsonProperty("do_sample")
    private Boolean doSample;
    /**
     * 采样温度，控制输出的随机性，必须为正数
     * 取值范围是：(0.0, 1.0)，不能等于 0，默认值为 0.95，值越大，会使输出更随机，更具创造性；值越小，输出会更加稳定或确定
     * 建议您根据应用场景调整 top_p 或 temperature 参数，但不要同时调整两个参数
     */
    @JsonProperty("temperature")
    private Float temperature;
    /**
     * 用温度取样的另一种方法，称为核取样取值范围是：(0.0, 1.0) 开区间，不能等于 0 或 1，默认值为 0.7
     * 模型考虑具有 top_p 概率质量 tokens 的结果
     * 例如：0.1 意味着模型解码器只考虑从前 10% 的概率的候选集中取 tokens
     * 建议您根据应用场景调整 top_p 或 temperature 参数，但不要同时调整两个参数
     */
    @JsonProperty("top_p")
    private Float topP;
    /**
     * 终端用户的唯一ID，协助平台对终端用户的违规行为、生成违法及不良信息或其他滥用行为进行干预。ID长度要求：最少6个字符，最多128个字符。
     */
    @JsonProperty(value = "user_id")
    private String user;
    /**
     * 模型在遇到stop所制定的字符时将停止生成，目前仅支持单个停止词，格式为["stop_word1"]
     */
    @JsonProperty("stop")
    private List<String> stop;

    /**
     * 所要调用的模型编码
     */
    @JsonProperty("model")
    private String model;

    /**
     * 可供模型调用的工具列表,tools 字段会计算 tokens ，同样受到 tokens 长度的限制
     */
    @NestedConfigurationProperty
    private @JsonProperty("tools") List<StepFunAiApi.FunctionTool> tools;

    /**
     * 用于控制模型是如何选择要调用的函数，仅当工具类型为function时补充。默认为auto，当前仅支持auto
     */
    @NestedConfigurationProperty
    private @JsonProperty("tool_choice") StepFunAiApi.ChatCompletionRequest.ToolChoice toolChoice;

    @Override
    public List<FunctionCallback> getFunctionCallbacks() {
        return null;
    }

    @Override
    public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {

    }

    @Override
    public Set<String> getFunctions() {
        return null;
    }

    @Override
    public void setFunctions(Set<String> functions) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final StepFunAiChatOptions options = new StepFunAiChatOptions();

        public Builder withModel(String model) {
            this.options.setModel(model);
            return this;
        }

        public Builder withMaxToken(Integer maxTokens) {
            this.options.setMaxTokens(maxTokens);
            return this;
        }

        public Builder withDoSample(Boolean doSample) {
            this.options.setDoSample(doSample);
            return this;
        }

        public Builder withTemperature(Float temperature) {
            this.options.setTemperature(temperature);
            return this;
        }

        public Builder withTopP(Float topP) {
            this.options.setTopP(topP);
            return this;
        }

        public Builder withUser(String user) {
            this.options.setUser(user);
            return this;
        }

        public Builder withStop(List<String> stop) {
            this.options.setStop(stop);
            return this;
        }

        public Builder withTools(List<StepFunAiApi.FunctionTool> tools) {
            this.options.setTools(tools);
            return this;
        }

        public Builder withToolChoice(StepFunAiApi.ChatCompletionRequest.ToolChoice toolChoice) {
            this.options.setToolChoice(toolChoice);
            return this;
        }

        public StepFunAiChatOptions build() {
            return this.options;
        }

    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getDoSample() {
        return doSample;
    }

    public void setDoSample(Boolean doSample) {
        this.doSample = doSample;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    @Override
    public Float getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    @Override
    public Float getTopP() {
        return this.topP;
    }

    public void setTopP(Float topP) {
        this.topP = topP;
    }

    @Override
    @JsonIgnore
    public Integer getTopK() {
        throw new UnsupportedOperationException("Unimplemented method 'getTopK'");
    }

    @JsonIgnore
    public void setTopK(Integer topK) {
        throw new UnsupportedOperationException("Unimplemented method 'setTopK'");
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public List<StepFunAiApi.FunctionTool> getTools() {
        return tools;
    }

    public void setTools(List<StepFunAiApi.FunctionTool> tools) {
        this.tools = tools;
    }

    public StepFunAiApi.ChatCompletionRequest.ToolChoice getToolChoice() {
        return toolChoice;
    }

    public void setToolChoice(StepFunAiApi.ChatCompletionRequest.ToolChoice toolChoice) {
        this.toolChoice = toolChoice;
    }

    /**
     * Convert the {@link StepFunAiChatOptions} object to a {@link Map} of key/value pairs.
     * @return The {@link Map} of key/value pairs.
     */
    public Map<String, Object> toMap() {
        try {
            var json = new ObjectMapper().writeValueAsString(this);
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper factory method to create a new {@link StepFunAiChatOptions} instance.
     * @return A new {@link StepFunAiChatOptions} instance.
     */
    public static StepFunAiChatOptions create() {
        return new StepFunAiChatOptions();
    }

    /**
     * Filter out the non supported fields from the options.
     * @param options The options to filter.
     * @return The filtered options.
     */
    public static Map<String, Object> filterNonSupportedFields(Map<String, Object> options) {
        return options.entrySet().stream()
                .filter(e -> !e.getKey().equals("model"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


}
