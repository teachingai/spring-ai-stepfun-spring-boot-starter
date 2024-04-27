package org.springframework.ai.stepfun.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.function.Consumer;

public class ApiUtils {
    public final static String DEFAULT_BASE_URL = "https://api.stepfun.com";

    public static final Integer DEFAULT_MAX_TOKENS = 1024;

    public static final Float DEFAULT_TEMPERATURE = 0.95f;

    public static final Float DEFAULT_TOP_P = 0.7f;

    public static Consumer<HttpHeaders> getJsonContentHeaders(String apiKey) {
        return (headers) -> {
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            // headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        };
    };

}
