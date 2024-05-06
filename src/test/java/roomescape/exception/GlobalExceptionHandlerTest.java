package roomescape.exception;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalExceptionHandlerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("이름에 공백이 입력될 경우 상태코드가 400이 된다.")
    void handleInvalidDateException() {
        // given
        Map<String, String> params = Map.of(
                "name", " ",
                "date", "1998-02-24",
                "timeId", "1",
                "themeId", "1"
        );

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
