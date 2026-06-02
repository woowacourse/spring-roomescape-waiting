package roomescape.common;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;

class GlobalExceptionHandlerTest {

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(handler)
                .build();
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @DisplayName("NotFoundException이 발생하면 404를 반환한다")
    void returnsNotFoundForNotFoundException() {
        RestAssuredMockMvc.given()
                .when().get("/test/not-found")
                .then()
                .status(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("ConflictException이 발생하면 409를 반환한다")
    void returnsConflictForConflictException() {
        RestAssuredMockMvc.given()
                .when().get("/test/conflict")
                .then()
                .status(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("잘못된 JSON 형식으로 요청하면 400을 반환한다")
    void returnsBadRequestForInvalidJson() {
        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{ invalid json }")
                .when().post("/test/validate")
                .then()
                .status(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Bean Validation 실패 시 400을 반환한다")
    void returnsBadRequestForValidationFailure() {
        FakeController.TestRequest testRequest = new FakeController.TestRequest(null);
        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(testRequest)
                .when().post("/test/validate")
                .then()
                .status(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Bean Validation 실패 시 invalid-params와 instance를 포함한다")
    void returnsInvalidParamsAndInstanceOnValidationFailure() {
        FakeController.TestRequest testRequest = new FakeController.TestRequest(null);
        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(testRequest)
                .when().post("/test/validate")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("title", equalTo("입력값 검증 실패"))
                .body("instance", equalTo("/test/validate"))
                .body("invalid-params", hasSize(1))
                .body("invalid-params[0].field", equalTo("value"))
                .body("invalid-params[0].reason", notNullValue());
    }

    @Test
    @DisplayName("BaseException 발생 시 ProblemDetail 구조로 응답한다")
    void returnsProblemDetailForBaseException() {
        RestAssuredMockMvc.given()
                .when().get("/test/not-found")
                .then()
                .status(HttpStatus.NOT_FOUND)
                .body("status", equalTo(404))
                .body("detail", equalTo("찾을 수 없음"));
    }

    @Test
    @DisplayName("처리되지 않은 예외가 발생하면 500을 반환하고 원인 메시지를 노출하지 않는다")
    void returnsInternalServerErrorForUnhandledException() {
        RestAssuredMockMvc.given()
                .when().get("/test/illegal-argument")
                .then()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("detail", equalTo("예상치 못한 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("Path variable 타입 불일치 시 400을 반환한다")
    void returnsBadRequestForTypeMismatch() {
        RestAssuredMockMvc.given()
                .when().get("/test/type-mismatch/not-a-number")
                .then()
                .status(HttpStatus.BAD_REQUEST);
    }

    @RestController
    static class FakeController {

        record TestRequest(@NotNull String value) {
        }

        @GetMapping("/test/not-found")
        void throwNotFoundException() {
            throw new EntityNotFoundException("찾을 수 없음");
        }

        @GetMapping("/test/conflict")
        void throwConflictException() {
            throw new DuplicateEntityException("충돌");
        }

        @GetMapping("/test/illegal-argument")
        void throwIllegalArgumentException() {
            throw new IllegalArgumentException("잘못된 인수");
        }

        @PostMapping("/test/validate")
        void validate(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/type-mismatch/{id}")
        void typeMismatch(@PathVariable Long id) {
        }
    }
}
