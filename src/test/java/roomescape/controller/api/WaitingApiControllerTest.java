package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
import roomescape.service.WaitingService;
import roomescape.service.dto.input.MemberCreateInput;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.input.WaitingInput;
import roomescape.service.dto.output.WaitingOutput;
import roomescape.util.DatabaseCleaner;
import roomescape.util.TokenProvider;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WaitingApiControllerTest {

    @Autowired
    WaitingService waitingService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ThemeService themeService;

    @Autowired
    MemberService memberService;

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
        token = tokenProvider.generateToken(
                Member.fromMember(output.id(), output.name(), output.email(), output.password()));
    }

    @Test
    @DisplayName("예약 대기 생성에 성공하면, 201을 반환한다")
    void return_201_when_waiting_create_success() {
        long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("14:00"))
                .id();
        long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();
        long memberId = memberService.createMember(MemberFixture.getUserCreateInput("new123@gmail.com"))
                .id();
        String date = LocalDate.now().plusDays(1).toString();
        reservationService.createReservation(new ReservationInput(date, timeId, themeId, memberId));

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("date", date);
        waiting.put("timeId", timeId);
        waiting.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(waiting)
                .when()
                .post("/waitings")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약 대기 삭제에 성공하면, 204를 반환한다")
    void return_204_when_waiting_delete_success() {
        long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("14:00"))
                .id();
        long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();
        long memberId1 = memberService.createMember(MemberFixture.getUserCreateInput("new123@gmail.com"))
                .id();
        long memberId2 = tokenProvider.decodeToken(token).getId();
        String date = LocalDate.now().plusDays(1).toString();
        reservationService.createReservation(new ReservationInput(date, timeId, themeId, memberId1));
        WaitingOutput output = waitingService.createWaiting(new WaitingInput(date, timeId, themeId, memberId2));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when()
                .delete("/waitings/" + output.id())
                .then()
                .statusCode(204);
    }
}
