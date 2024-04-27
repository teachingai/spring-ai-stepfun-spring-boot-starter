package org.springframework.ai.stepfun.autoconfigure;


import org.springframework.ai.stepfun.util.ApiUtils;

class StepFunAiParentProperties {

    /**
     * Base URL where 阶跃星辰 API server is running.
     */
    private String baseUrl = ApiUtils.DEFAULT_BASE_URL;

    private String apiKey;

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
