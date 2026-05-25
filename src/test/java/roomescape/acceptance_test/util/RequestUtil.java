package roomescape.acceptance_test.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public final class RequestUtil {

    private RequestUtil() {
    }

    public static ExtractableResponse<Response> get(String path) {
        return get(path, Map.of(), Map.of());
    }

    public static ExtractableResponse<Response> get(
            String path,
            Map<String, ?> queryParams
    ) {
        return get(path, queryParams, Map.of());
    }

    public static ExtractableResponse<Response> get(
            String path,
            Map<String, ?> queryParams,
            Map<String, ?> headers
    ) {
        return given().log().all()
                .headers(headers)
                .queryParams(queryParams)
                .when()
                .get(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> post(
            ObjectMapper objectMapper,
            String path,
            Object body
    ) throws JsonProcessingException {
        return post(objectMapper, path, body, Map.of(), Map.of());
    }

    public static ExtractableResponse<Response> post(
            ObjectMapper objectMapper,
            String path,
            Object body,
            Map<String, ?> pathParams,
            Map<String, ?> headers
    ) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .headers(headers)
                .pathParams(pathParams)
                .body(objectMapper.writeValueAsString(body))
                .when()
                .post(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> patch(
            ObjectMapper objectMapper,
            String path,
            Object body,
            Map<String, ?> pathParams,
            Map<String, ?> headers
    ) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .headers(headers)
                .pathParams(pathParams)
                .body(objectMapper.writeValueAsString(body))
                .when()
                .patch(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> delete(
            String path,
            Map<String, ?> pathParams
    ) {
        return delete(path, pathParams, Map.of());
    }

    public static ExtractableResponse<Response> delete(
            String path,
            Map<String, ?> pathParams,
            Map<String, ?> headers
    ) {
        return given().log().all()
                .headers(headers)
                .pathParams(pathParams)
                .when()
                .delete(path)
                .then().log().all()
                .extract();
    }
}
