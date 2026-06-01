package roomescape.common.advice;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;
import roomescape.common.advice.GlobalExceptionHandler;

@WebMvcTest
@ContextConfiguration(classes = {GlobalExceptionHandler.class, DummyController.class})
class RestExceptionHandlerTest {

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    void 정확한_요청이면_200_OK를_응답한다() {
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
    void 요청_JSON_형식이_잘못되면_구조화된_에러_응답을_반환한다() {
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
    void 필수_필드가_누락되면_구조화된_에러_응답을_반환한다() {
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
    void 지원하지_않는_HTTP_메서드면_구조화된_에러_응답을_반환한다() {
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
    void 경로_변수_검증에_실패하면_구조화된_에러_응답을_반환한다() {
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
    void 커스텀_예외는_지정한_상태와_메시지로_응답한다() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/business")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("BUSINESS_ERROR"))
                .body("message", equalTo("비즈니스 예외"));
    }

    @Test
    void 접근_권한_예외는_403_FORBIDDEN을_응답한다() {
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
    void 엔티티를_찾지_못하면_404_NOT_FOUND를_응답한다() {
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
    void 엔티티가_충돌하면_409_CONFLICT를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/duplicateEntity")
                .then().log().all()
                .status(HttpStatus.CONFLICT)
                .body("code", equalTo("CONFLICT"))
                .body("message", equalTo("충돌"));
    }

    @Test
    void 필수_요청_파라미터가_누락되면_구조화된_에러_응답을_반환한다() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .when().get("/dummy/param")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("MISSING_REQUEST_PARAMETER"))
                .body("message", equalTo("test 파라미터가 누락 되었습니다."));
    }

    @Test
    void 처리하지_않은_예외는_500_INTERNAL_SERVER_ERROR를_응답한다() {
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
