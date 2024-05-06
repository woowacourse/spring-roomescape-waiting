package roomescape.controller.time;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimeControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @ParameterizedTest
    @MethodSource("invalidRequestParameterProvider")
    @DisplayName("유효하지 않는 요청인 경우 400을 반환한다.")
    void invalidRequest(final String startAt) {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    static Stream<Arguments> invalidRequestParameterProvider() {
        return Stream.of(
                Arguments.of("20202020"),
                Arguments.of("1234"),
                Arguments.of("2026-"),
                Arguments.of("2026-13-01"),
                Arguments.of("2026-12-32"),
                Arguments.of("2026-11"),
                Arguments.of("2026-1")
        );
    }
}
