package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.input.MemberCreateInput;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.util.TokenProvider;
import roomescape.util.DatabaseCleaner;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationApiControllerTest {

    @Autowired
    ReservationService reservationService;
    @Autowired
    ThemeService themeService;
    @Autowired
    MemberService memberService;
    @Autowired
    ReservationTimeService reservationTimeService;
    @Autowired
    DatabaseCleaner databaseCleaner;

    @LocalServerPort
    int port;
    @Autowired
    TokenProvider tokenProvider;

    String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.initialize();
        final var output = memberService.createMember(new MemberCreateInput("조이선", "joyson@gmail.com", "password1234"));
        token = tokenProvider.generateToken(Member.fromMember(output.id(), output.name(), output.email(), output.password()));
    }

    @Test
    @DisplayName("예약 생성에 성공하면, 201을 반환한다")
    void return_200_when_reservation_create_success() {
        long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("14:00"))
                .id();
        long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2024-08-05");
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when()
                .post("/reservations")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력되었을 때 400을 반환한다.")
    void return_400_when_reservation_create_input_is_invalid() {
        long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("12:00"))
                .id();
        long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2023.08.05");
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when()
                .post("/reservations")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("특정 예약이 존재하지 않는데, 그 예약을 삭제하려 할 때 404을 반환한다.")
    void return_404_when_not_exist_id() {
        RestAssured.given()
                .cookie("token", token)
                .delete("/reservations/-1")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("중복된 예약을 생성하려 할 때 409를 반환한다.")
    void return_409_when_duplicate_reservation() {
        long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("13:00"))
                .id();
        long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        reservationService.createReservation(new ReservationInput("2024-06-30", timeId, themeId, memberId));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2024-06-30");
        reservation.put("timeId", themeId);
        reservation.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when()
                .post("/reservations")
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("지나간 날짜와 시간으로 예약 생성 시 400를 반환한다.")
    void return_400_when_create_past_time_reservation() {
        long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("03:00"))
                .id();
        long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "1024-03-30");
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when()
                .post("/reservations")
                .then()
                .statusCode(400);
    }
}
