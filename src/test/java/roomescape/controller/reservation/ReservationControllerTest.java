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
import org.springframework.test.annotation.DirtiesContext;
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
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
                .body(new MemberLoginRequest("jinwuo0925@gmail.com", "1111"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("예약 조회 (예약 대기 제외)")
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
                                "18:00", false), new ReservationThemeResponse("가을")),
                new ReservationResponse(7L, new MemberResponse("재즈"), today.plusDays(4).toString(),
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

    @Test
    @DisplayName("나의 예약 조회")
    void getReservationMine() {
        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @ParameterizedTest
    @DisplayName("예약 조건 조회")
    @MethodSource("searchReservationsParameterProvider")
    void searchReservations(final Map<String, Object> param, final List<Long> expected) {
        final List<ReservationResponse> result = RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .params(param)
                .when().get("/reservations/search")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$", ReservationResponse.class);

        final List<Long> ids = result.stream()
                .map(ReservationResponse::id)
                .toList();

        assertThat(ids).isEqualTo(expected);
    }

    @Test
    @DisplayName("예약 대기 삭제")
    void deleteWaitReservation() {
        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .when().delete("/reservations/wait/8")
                .then().log().all()
                .statusCode(204);
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

    static Stream<Arguments> searchReservationsParameterProvider() {
        final LocalDate date = LocalDate.now();

        return Stream.of(
                Arguments.of(Map.of("themeId", 2L, "memberId", 2L, "dateFrom", String.valueOf(date.minusDays(8)),
                                "dateTo", String.valueOf(date.minusDays(1))),
                        List.of(2L, 4L)),
                Arguments.of(Map.of("themeId", 3L, "memberId", 3L, "dateFrom", String.valueOf(date.minusDays(5)),
                                "dateTo", String.valueOf(date.plusDays(5))),
                        List.of(6L, 8L)),
                Arguments.of(Map.of("themeId", 3L, "memberId", 3L, "dateFrom", String.valueOf(date.minusDays(7)),
                                "dateTo", String.valueOf(date.plusDays(4))),
                        List.of(5L, 6L, 8L)),
                Arguments.of(Map.of("themeId", 1L, "memberId", 1L, "dateFrom", String.valueOf(date.minusDays(14)),
                                "dateTo", String.valueOf(date.minusDays(7))),
                        List.of())
        );
    }
}
