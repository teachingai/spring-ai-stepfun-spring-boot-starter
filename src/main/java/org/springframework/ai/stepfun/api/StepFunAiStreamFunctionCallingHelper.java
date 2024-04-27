package org.springframework.ai.stepfun.api;

import org.springframework.util.CollectionUtils;

import java.util.*;

public class StepFunAiStreamFunctionCallingHelper {

    /**
     * Merge the previous and current ChatCompletionChunk into a single one.
     * @param previous the previous ChatCompletionChunk
     * @param current the current ChatCompletionChunk
     * @return the merged ChatCompletionChunk
     */
    public StepFunAiApi.ChatCompletionChunk merge(StepFunAiApi.ChatCompletionChunk previous, StepFunAiApi.ChatCompletionChunk current) {

        if (previous == null) {
            return current;
        }

        String id = (current.id() != null ? current.id() : previous.id());
        Long created = (current.created() != null ? current.created() : previous.created());
        String model = (current.model() != null ? current.model() : previous.model());
        String requestId = (current.requestId() != null ? current.requestId() : previous.requestId());
        String object = (current.object() != null ? current.object() : previous.object());

        StepFunAiApi.ChatCompletionChunk.ChunkChoice previousChoice0 = (CollectionUtils.isEmpty(previous.choices()) ? null : previous.choices().get(0));
        StepFunAiApi.ChatCompletionChunk.ChunkChoice currentChoice0 = (CollectionUtils.isEmpty(current.choices()) ? null : current.choices().get(0));

        StepFunAiApi.ChatCompletionChunk.ChunkChoice choice = merge(previousChoice0, currentChoice0);

        return new StepFunAiApi.ChatCompletionChunk(id, object, created, model, requestId, List.of(choice));
    }

    private StepFunAiApi.ChatCompletionChunk.ChunkChoice merge(StepFunAiApi.ChatCompletionChunk.ChunkChoice previous, StepFunAiApi.ChatCompletionChunk.ChunkChoice current) {
        if (previous == null) {
            if (current.delta() != null && current.delta().toolCalls() != null) {
                Optional<String> id = current.delta()
                        .toolCalls()
                        .stream()
                        .map(StepFunAiApi.ChatCompletionMessage.ToolCall::id)
                        .filter(Objects::nonNull)
                        .findFirst();
                if (id.isEmpty()) {
                    var newId = UUID.randomUUID().toString();

                    var toolCallsWithID = current.delta()
                            .toolCalls()
                            .stream()
                            .map(toolCall -> new StepFunAiApi.ChatCompletionMessage.ToolCall(newId, "function", toolCall.function()))
                            .toList();

                    var role = current.delta().role() != null ? current.delta().role() : StepFunAiApi.ChatCompletionMessage.Role.ASSISTANT;
                    current = new StepFunAiApi.ChatCompletionChunk.ChunkChoice(current.index(), new StepFunAiApi.ChatCompletionMessage(current.delta().content(),
                            role, current.delta().name(), toolCallsWithID), current.finishReason());
                }
            }
            return current;
        }

        StepFunAiApi.ChatCompletionFinishReason finishReason = (current.finishReason() != null ? current.finishReason()
                : previous.finishReason());
        Integer index = (current.index() != null ? current.index() : previous.index());

        StepFunAiApi.ChatCompletionMessage message = merge(previous.delta(), current.delta());

        return new StepFunAiApi.ChatCompletionChunk.ChunkChoice(index, message, finishReason);
    }

    private StepFunAiApi.ChatCompletionMessage merge(StepFunAiApi.ChatCompletionMessage previous, StepFunAiApi.ChatCompletionMessage current) {
        String content = (current.content() != null ? current.content()
                : "" + ((previous.content() != null) ? previous.content() : ""));
        StepFunAiApi.ChatCompletionMessage.Role role = (current.role() != null ? current.role() : previous.role());
        role = (role != null ? role : StepFunAiApi.ChatCompletionMessage.Role.ASSISTANT); // default to ASSISTANT (if null
        String name = (current.name() != null ? current.name() : previous.name());

        List<StepFunAiApi.ChatCompletionMessage.ToolCall> toolCalls = new ArrayList<>();
        StepFunAiApi.ChatCompletionMessage.ToolCall lastPreviousTooCall = null;
        if (previous.toolCalls() != null) {
            lastPreviousTooCall = previous.toolCalls().get(previous.toolCalls().size() - 1);
            if (previous.toolCalls().size() > 1) {
                toolCalls.addAll(previous.toolCalls().subList(0, previous.toolCalls().size() - 1));
            }
        }
        if (current.toolCalls() != null) {
            if (current.toolCalls().size() > 1) {
                throw new IllegalStateException("Currently only one tool call is supported per message!");
            }
            var currentToolCall = current.toolCalls().iterator().next();
            if (currentToolCall.id() != null) {
                if (lastPreviousTooCall != null) {
                    toolCalls.add(lastPreviousTooCall);
                }
                toolCalls.add(currentToolCall);
            }
            else {
                toolCalls.add(merge(lastPreviousTooCall, currentToolCall));
            }
        }
        else {
            if (lastPreviousTooCall != null) {
                toolCalls.add(lastPreviousTooCall);
            }
        }
        return new StepFunAiApi.ChatCompletionMessage(content, role, name, toolCalls);
    }

    private StepFunAiApi.ChatCompletionMessage.ToolCall merge(StepFunAiApi.ChatCompletionMessage.ToolCall previous, StepFunAiApi.ChatCompletionMessage.ToolCall current) {
        if (previous == null) {
            return current;
        }
        String id = (current.id() != null ? current.id() : previous.id());
        String type = (current.type() != null ? current.type() : previous.type());
        StepFunAiApi.ChatCompletionMessage.ChatCompletionFunction function = merge(previous.function(), current.function());
        return new StepFunAiApi.ChatCompletionMessage.ToolCall(id, type, function);
    }

    private StepFunAiApi.ChatCompletionMessage.ChatCompletionFunction merge(StepFunAiApi.ChatCompletionMessage.ChatCompletionFunction previous, StepFunAiApi.ChatCompletionMessage.ChatCompletionFunction current) {
        if (previous == null) {
            return current;
        }
        String name = (current.name() != null ? current.name() : previous.name());
        StringBuilder arguments = new StringBuilder();
        if (previous.arguments() != null) {
            arguments.append(previous.arguments());
        }
        if (current.arguments() != null) {
            arguments.append(current.arguments());
        }
        return new StepFunAiApi.ChatCompletionMessage.ChatCompletionFunction(name, arguments.toString());
    }

    /**
     * @param chatCompletion the ChatCompletionChunk to check
     * @return true if the ChatCompletionChunk is a streaming tool function call.
     */
    public boolean isStreamingToolFunctionCall(StepFunAiApi.ChatCompletionChunk chatCompletion) {

        var choices = chatCompletion.choices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        var choice = choices.get(0);
        return !CollectionUtils.isEmpty(choice.delta().toolCalls());
    }

    /**
     * @param chatCompletion the ChatCompletionChunk to check
     * @return true if the ChatCompletionChunk is a streaming tool function call and it is
     * the last one.
     */
    public boolean isStreamingToolFunctionCallFinish(StepFunAiApi.ChatCompletionChunk chatCompletion) {

        var choices = chatCompletion.choices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        var choice = choices.get(0);
        return choice.finishReason() == StepFunAiApi.ChatCompletionFinishReason.TOOL_CALLS;
    }

}
