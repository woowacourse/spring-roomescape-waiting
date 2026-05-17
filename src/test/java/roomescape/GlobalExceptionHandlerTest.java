package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ProblemType;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class GlobalExceptionHandlerTest {

    @Test
    void 경로_변수_타입_불일치는_400() {
        RestAssured.given().log().all()
                .when().delete("/reservations/abc")
                .then().log().all()
                .statusCode(400)
                .body("type", is(ProblemType.BAD_REQUEST.uri().toString()));
    }

    @Test
    void 지원하지_않는_HTTP_메서드는_405() {
        RestAssured.given().log().all()
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(405)
                .body("type", is(ProblemType.METHOD_NOT_SUPPORTED.uri().toString()));
    }

    @Test
    void 지원하지_않는_미디어_타입은_415() {
        RestAssured.given().log().all()
                .contentType(ContentType.TEXT)
                .body("not json")
                .when().post("/reservations")
                .then().log().all()
                .statusCode(415)
                .body("type", is(ProblemType.MEDIA_TYPE_NOT_SUPPORTED.uri().toString()));
    }
}
