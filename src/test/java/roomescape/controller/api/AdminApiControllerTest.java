package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.service.MemberService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.util.TokenProvider;
import roomescape.util.DatabaseCleaner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiControllerTest {
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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("예약 생성에 성공하면, 201을 반환한다")
    void return_201_when_reservation_create_success() {
        final long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("14:00"))
                .id();
        final long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();
        final var output = memberService.createMember(MemberFixture.getAdminCreateInput());
        final String token = tokenProvider.generateToken(Member.fromMember(output.id(), output.name(), output.email(), output.password()));


        final Map<String, Object> reservation = new HashMap<>();
        reservation.put("memberId",output.id());
        reservation.put("date", "2024-08-05");
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when()
                .post("/admin/reservations")
                .then()
                .statusCode(201);
    }
}
