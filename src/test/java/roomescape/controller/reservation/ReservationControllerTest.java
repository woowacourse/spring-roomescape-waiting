package roomescape.controller.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationControllerTest {

    @Autowired
    ReservationController reservationController;

    @LocalServerPort
    int port;

    String accessToken;

    static Stream<Arguments> invalidRequestParameterProvider() {
        final String date = LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_DATE);
        final String timeId = "1";
        final String themeId = "1";

        return Stream.of(
                Arguments.of(date, "dk", themeId),
                Arguments.of(date, timeId, "al"),
                Arguments.of("2023", timeId, themeId)
        );
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        accessToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("redddy@gmail.com", "0000"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("예약 조회")
    void getReservations() {
        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(6));
    }

    @ParameterizedTest
    @MethodSource("invalidRequestParameterProvider")
    @DisplayName("유효하지 않은 요청인 경우 400을 반환한다.")
    void invalidRequest(final String date, final String timeId, final String themeId) {
        final Map<String, String> params = Map.of(
                "date", date,
                "timeId", timeId,
                "themeId", themeId);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
