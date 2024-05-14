package roomescape.controller.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import roomescape.controller.reservation.dto.MemberResponse;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.theme.dto.ReservationThemeResponse;
import roomescape.controller.time.dto.AvailabilityTimeResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationControllerTest {

    @Autowired
    ReservationController reservationController;

    @LocalServerPort
    int port;

    String accessToken;

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
        final List<ReservationResponse> reservations = reservationController.getReservations();
        final LocalDate today = LocalDate.now();

        final List<ReservationResponse> expected = List.of(
                new ReservationResponse(1L, new MemberResponse("레디"), today.minusDays(3).toString(),
                        new AvailabilityTimeResponse(1L,
                                "15:00", false), new ReservationThemeResponse("봄")),
                new ReservationResponse(2L, new MemberResponse("재즈"), today.minusDays(2).toString(),
                        new AvailabilityTimeResponse(3L,
                                "17:00", false), new ReservationThemeResponse("여름")),
                new ReservationResponse(3L, new MemberResponse("레디"), today.minusDays(1).toString(),
                        new AvailabilityTimeResponse(2L,
                                "16:00", false), new ReservationThemeResponse("여름")),
                new ReservationResponse(4L, new MemberResponse("재즈"), today.minusDays(1).toString(),
                        new AvailabilityTimeResponse(1L,
                                "15:00", false), new ReservationThemeResponse("여름")),
                new ReservationResponse(5L, new MemberResponse("제제"), today.minusDays(7).toString(),
                        new AvailabilityTimeResponse(1L,
                                "15:00", false), new ReservationThemeResponse("가을")),
                new ReservationResponse(6L, new MemberResponse("제제"), today.plusDays(3).toString(),
                        new AvailabilityTimeResponse(4L,
                                "18:00", false), new ReservationThemeResponse("가을"))
        );
        assertThat(reservations).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("invalidRequestParameterProvider")
    @DisplayName("유효하지 않은 요청인 경우 400을 반환한다.")
    void invalidRequest(final String date, final String timeId, final String themeId) {
        final Map<String, String> params = Map.of("date", date, "timeId", timeId, "themeId", themeId);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

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
}
