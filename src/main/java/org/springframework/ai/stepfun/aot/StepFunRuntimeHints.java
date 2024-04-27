package org.springframework.ai.stepfun.aot;

import org.springframework.ai.stepfun.api.StepFunAiApi;
import org.springframework.ai.stepfun.api.StepFunAiChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class StepFunRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(StepFunAiApi.class)) {
            hints.reflection().registerType(tr, mcs);
        }
        for (var tr : findJsonAnnotatedClassesInPackage(StepFunAiChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
