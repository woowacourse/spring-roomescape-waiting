package roomescape.global;


import static org.hamcrest.Matchers.containsString;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(DummyController.class)
public class RestExceptionHandlerTest {

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    void 정확한_요청시_200_OK() {
        // given: 올바른 요청
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
    void 잘못된_요청_형식_테스트() {
        // given: 요청 형식에 comma 포함
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
                .body(containsString("요청 Json 형식이 잘못되었습니다."));
    }

    @Test
    void 필드가_누락된_경우_null_체크_검증() {
        // given: 요청 형식에 필드 누락
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
                .body(containsString("[testField] 필드 낫널 검증"));
    }

    @Test
    void 존재하지_않는_Method로_API_요청() {
        // given: 올바른 요청
        String body = """
                {
                    "testField": "1234"
                }
                """;

        // when & then: post 메서드가 아닌 delete 메서드로 요청
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when().delete("/dummy")
                .then().log().all()
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(containsString("지원하지 않는 HTTP Method 입니다."));
    }

    @Test
    void constraint_테스트() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().post("/dummy/0")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("양수가 아님"));
    }

    @Test
    void path_variable_타입_변환_실패() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().post("/dummy/string")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("변환할 수 없는 잘못된 데이터 타입이 존재합니다."));
    }

    @Test
    void 비즈니스_예외() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/business")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("비즈니스 예외"));
    }

    @Test
    void 엔티티_못찾을_경우_상태_404() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/entityNotFound")
                .then().log().all()
                .status(HttpStatus.NOT_FOUND)
                .body(containsString("데이터 없음"));
    }

    @Test
    void 엔티티_충돌할_경우_409() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/duplicateEntity")
                .then().log().all()
                .status(HttpStatus.CONFLICT)
                .body(containsString("충돌"));
    }

    @Test
    void 파라미터가_누락된_경우_400() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/param")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("test 파라미터가 누락 되었습니다."));
    }

    @Test
    void 나머지_예외는_서버_예외() {
        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(MediaType.APPLICATION_JSON)
                .when().get("/dummy/internal")
                .then().log().all()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(containsString("알 수 없는 서버 예외가 발생했습니다."));
    }
}
