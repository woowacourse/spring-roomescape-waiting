package roomescape.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exceptionHandler.dto.ExceptionResponse;
import roomescape.common.util.JwtTokenContainer;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = "/data/adminIntegrationTest.sql")
public class AdminIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenContainer jwtTokenContainer;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("/admin 요청 시 admin/index.html과 200 응답을 준다.")
    void when_admin_request() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("/admin/reservation 요청 시 html 정상 응답과 예약 목록을 요청한다.")
    void when_admin_reservation_request() {
        RestAssured.given().log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @Test
    @DisplayName("예약 대기만 가져온다.")
    void getWaitingReservation_test() {
        // given
        Member admin = em.find(Member.class, 3L);
        String jwtToken = jwtTokenContainer.createJwtToken(admin, LocalDateTime.now());
        LocalDate date = LocalDate.now().plusDays(1);
        // when & thene
        RestAssured.given().log().all()
                .cookie("token", jwtToken)
                .when().get("/admin/waiting-reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(5))
                .body("[0].name", equalTo("율무"))
                .body("[0].theme", equalTo("테마1"))
                .body("[0].date", equalTo(date.toString()))
                .body("[0].startAt", equalTo("11:00:00"));
    }

    @Test
    @DisplayName("정상적으로 예약을 삭제한다.")
    void deleteReservation_test() {
        // given
        Member admin = em.find(Member.class, 3L);
        String jwtToken = jwtTokenContainer.createJwtToken(admin, LocalDateTime.now());
        // when & then
        RestAssured.given().log().all()
                .cookie("token", jwtToken)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);
        Reservation reservation = em.find(Reservation.class, 1L);
        assertThat(reservation).isNull();
    }

    @Test
    @DisplayName("정상적으로 예약 대기를 예약 확정으로 변경한다.")
    void changeReservationStatusToReserved_test() {
        // given
        Member admin = em.find(Member.class, 3L);
        String jwtToken = jwtTokenContainer.createJwtToken(admin, LocalDateTime.now());
        RestAssured.given().log().all()
                .cookie("token", jwtToken)
                .when().delete("/admin/reservations/2")
                .then().log().all()
                .statusCode(204);
        // when & then
        RestAssured.given().log().all()
                .cookie("token", jwtToken)
                .when().patch("/admin/reservations/5")
                .then().log().all()
                .statusCode(200);
        Reservation reservation = em.find(Reservation.class, 5L);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("기존 예약이 취소되지 않았는데 예약 확정으로 바꾸려고하면 예외가 발생한다.")
    void changeReservationStatusToReserved_not_cancel_yet_exception() {
        // given
        Member admin = em.find(Member.class, 3L);
        String jwtToken = jwtTokenContainer.createJwtToken(admin, LocalDateTime.now());
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 기존 확정 예약이 취소 되지 않았습니다.", "/admin/reservations/5");
        // when & then
        Response response = RestAssured.given().log().all()
                .cookie("token", jwtToken)
                .when().patch("/admin/reservations/5")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않은 예약의 상태를 바꾸려고 하면 예외가 발생한다.")
    void changeReservationStatusToReserved_no_reservation_exception() {
        // given
        Member admin = em.find(Member.class, 3L);
        String jwtToken = jwtTokenContainer.createJwtToken(admin, LocalDateTime.now());
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 존재하지 않는 예약대기입니다.", "/admin/reservations/100");
        // when & then
        Response response = RestAssured.given().log().all()
                .cookie("token", jwtToken)
                .when().patch("/admin/reservations/100")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }
}
