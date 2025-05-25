package roomescape.integrate.domain.reserveticket;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.integrate.fixture.RequestFixture;
import roomescape.service.reserveticket.ReserveTicketService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationWaitingTest {

    private final RequestFixture requestFixture = new RequestFixture();

    @LocalServerPort
    private int port;

    @Autowired
    private ReserveTicketService reserveTicketService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("대기 예약을 생성한다.")
    void addWaitingReservation() throws Exception {
        requestFixture.reqeustSignup("praisebak", "praisebak@naver.com", "asdfasdf");
        Map<String, String> cookies = requestFixture.requestLogin("praisebak@naver.com", "asdfasdf");

        LocalTime now = LocalTime.now();

        long timeId = requestFixture.requestAddTime(now.toString());
        long themeId = requestFixture.requestAddTheme("테마", "ㅁㄴㅇㄹ", "ㅁㄴㅇㄹ");

        AddReservationDto addReservationDto = new AddReservationDto(
                LocalDate.of(2099, 3, 5),
                timeId,
                themeId
        );

        long reservationId = requestFixture.requestAddReservation("asdf", LocalDate.of(2099, 3, 5).toString(), themeId,
                themeId,
                cookies);

        given()
                .contentType(ContentType.JSON)
                .cookie("token", cookies.get("token"))
                .body(addReservationDto)
                .when()
                .post("/reservations/waiting")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("대기 예약을 예약으로 변경한다.")
    void changeWaitingToReservation() {
        LocalTime now = LocalTime.now();
        long timeId = requestFixture.requestAddTime(now.toString());
        long themeId = requestFixture.requestAddTheme("테마", "ㅁㄴㅇㄹ", "ㅁㄴㅇㄹ");
        requestFixture.reqeustSignup("praisebak", "asdfasdf@naver.com", "asdfasdf");
        Map<String, String> cookies = requestFixture.requestLogin("asdfasdf@naver.com", "asdfasdf");

        AddReservationDto addReservationDto = new AddReservationDto(
                LocalDate.of(2099, 3, 5),
                timeId,
                themeId
        );

        long reservationId = requestFixture.requestAddReservation("asdf", LocalDate.of(2099, 3, 5).toString(), themeId,
                themeId,
                cookies);
        long waitingReservationId = reserveTicketService.addWaitingReservation(addReservationDto, 1L);
        reserveTicketService.deleteReservation(reservationId);

        given()
                .when()
                .post("/reservations/waiting/{id}", waitingReservationId)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("대기 예약을 삭제한다.")
    void removeWaitingReservation() {
        LocalTime now = LocalTime.now();
        long timeId = requestFixture.requestAddTime(now.toString());
        long themeId = requestFixture.requestAddTheme("테마", "ㅁㄴㅇㄹ", "ㅁㄴㅇㄹ");
        requestFixture.reqeustSignup("praisebak", "asdfasdf@naver.com", "asdfasdf");
        Map<String, String> cookies = requestFixture.requestLogin("asdfasdf@naver.com", "asdfasdf");

        AddReservationDto addReservationDto = new AddReservationDto(
                LocalDate.of(2099, 3, 5),
                timeId,
                themeId
        );

        requestFixture.requestAddReservation("asdf", LocalDate.of(2099, 3, 5).toString(), themeId, themeId, cookies);
        long waitingReservationId = reserveTicketService.addWaitingReservation(addReservationDto, 1L);

        given()
                .when()
                .delete("/reservations/waiting/{id}", waitingReservationId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("존재하지 않는 대기 예약을 예약으로 변경하려고 하면 실패한다.")
    void changeNonExistentWaitingToReservation() throws Exception {
        // when & then
        given()
                .when()
                .post("/reservations/waiting/{id}", 999L)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
