package roomescape.common.advice;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest
@ContextConfiguration(classes = {GlobalExceptionHandler.class, DummyController.class})
class GlobalExceptionHandlerTest {

    @BeforeEach
    void setUp(final WebApplicationContext webApplicationContext) {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    @DisplayName("정확한 요청이면 200 OK를 응답한다.")
    void validRequest() {
        // given
        String body = """
                {
                    "testField": "1234"
                }
                """;

        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when().post("/dummy")
                .then().log().all()
                .status(HttpStatus.OK)
                .body(containsString("1234"));
    }

    @Test
    @DisplayName("요청 JSON 형식이 잘못되면 구조화된 에러 응답을 반환한다.")
    void invalidJson() {
        // given
        String body = """
                {
                    "testField": "1234",
                }
                """;

        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when().post("/dummy")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("INVALID_JSON"))
                .body("message", equalTo("요청 Json 형식이 잘못되었습니다."));
    }

    @Test
    @DisplayName("필수 필드가 누락되면 구조화된 에러 응답을 반환한다.")
    void invalidRequestBody() {
        // given
        String body = """
                {
                }
                """;

        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when().post("/dummy")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("INVALID_REQUEST_BODY"))
                .body("message", equalTo("[testField] 필드 not null 검증"));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드면 구조화된 에러 응답을 반환한다.")
    void methodNotAllowed() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().delete("/dummy")
                .then().log().all()
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("code", equalTo("METHOD_NOT_ALLOWED"))
                .body("message", equalTo("지원하지 않는 HTTP Method 입니다."));
    }

    @Test
    @DisplayName("경로 변수 검증에 실패하면 구조화된 에러 응답을 반환한다.")
    void constraintViolation() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().post("/dummy/0")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("CONSTRAINT_VIOLATION"))
                .body("message", equalTo("양수가 아님"));
    }

    @Test
    @DisplayName("경로 변수 타입 변환에 실패하면 구조화된 에러 응답을 반환한다.")
    void typeMismatch() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().post("/dummy/string")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("TYPE_MISMATCH"))
                .body("message", equalTo("변환할 수 없는 잘못된 데이터 타입이 존재합니다."));
    }

    @Test
    @DisplayName("커스텀 예외는 지정한 상태와 메시지로 응답한다.")
    void customException() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/business")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("BUSINESS_EXCEPTION"))
                .body("message", equalTo("비즈니스 예외"));
    }

    @Test
    @DisplayName("접근 권한 예외는 403 FORBIDDEN을 응답한다.")
    void forbidden() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/forbidden")
                .then().log().all()
                .status(HttpStatus.FORBIDDEN)
                .body("code", equalTo("FORBIDDEN"))
                .body("message", equalTo("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("엔티티를 찾지 못하면 404 NOT_FOUND를 응답한다.")
    void notFound() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/entityNotFound")
                .then().log().all()
                .status(HttpStatus.NOT_FOUND)
                .body("code", equalTo("NOT_FOUND"))
                .body("message", equalTo("데이터 없음"));
    }

    @Test
    @DisplayName("엔티티가 충돌하면 409 CONFLICT를 응답한다.")
    void duplicate() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/duplicateEntity")
                .then().log().all()
                .status(HttpStatus.CONFLICT)
                .body("code", equalTo("DUPLICATE"))
                .body("message", equalTo("충돌"));
    }

    @Test
    @DisplayName("필수 요청 파라미터가 누락되면 구조화된 에러 응답을 반환한다.")
    void missingRequestParameter() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .when().get("/dummy/param")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("MISSING_REQUEST_PARAMETER"))
                .body("message", equalTo("test 파라미터가 누락 되었습니다."));
    }

    @Test
    @DisplayName("잘못된 경로면 404 NOT_FOUND를 응답한다.")
    void noResourceFound() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/illegalPath")
                .then().log().all()
                .status(HttpStatus.NOT_FOUND)
                .body("code", equalTo("NO_RESOURCE_FOUND"))
                .body("message", equalTo("존재하지 않는 경로입니다."));
    }

    @Test
    @DisplayName("처리하지 않은 예외는 500 INTERNAL_SERVER_ERROR를 응답한다.")
    void internalServerError() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/internal")
                .then().log().all()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("code", equalTo("INTERNAL_SERVER_ERROR"))
                .body("message", equalTo("알 수 없는 서버 예외가 발생했습니다."));
    }
}
