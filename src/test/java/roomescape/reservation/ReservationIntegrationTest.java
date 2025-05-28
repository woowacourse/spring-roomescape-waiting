package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exceptionHandler.dto.ExceptionResponse;
import roomescape.common.util.JwtTokenContainer;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationStatus;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = "/data/reservationIntegrationTest.sql")
public class ReservationIntegrationTest {

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

    @DisplayName("날짜가 null인 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_getDate() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", null);
        reservation.put("timeId", 1);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 날짜는 null 일 수 없습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 날짜로 생성 요청 시 400 응답을 준다.")
    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "123", "2월 5일", "2014년 2월 5일", "2023:12:03", "2024-15-10"})
    void when_given_wrong_getDate(final String date) {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", date);
        reservation.put("timeId", 1);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 요청 날짜 형식이 맞지 않습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 예약 시간 번호로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_wrong_time_get_id() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2024-12-03");
        reservation.put("timeId", "a");
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 요청 입력이 잘못되었습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 시간 번호가 null인 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_time_get_id() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2024-12-03");
        reservation.put("timeId", null);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 예약 시간 번호는 null 일 수 없습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("마이 페이지를 반환한다.")
    void get_my_page() {
        RestAssured.given().log().all()
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void createWaitingReservation() {
        // given
        Member member = em.find(Member.class, 1L);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        Map<String, Object> request = new HashMap<>();
        LocalDate date = LocalDate.now().plusDays(1);
        request.put("date", date.toString());
        request.put("time", 1);
        request.put("theme", 2);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtToken)
                .body(request)
                .when().post("/waiting-reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo(ReservationStatus.WAITED.getStatus()))
                .body("member.name", equalTo("코기"))
                .body("date", equalTo(date.toString()))
                .body("time.id", equalTo(1))
                .body("time.startAt", equalTo("10:00:00"))
                .body("theme.id", equalTo(2))
                .body("theme.name", equalTo("테마2"))
                .body("theme.description", equalTo("무서움"))
                .body("theme.thumbnail", equalTo("/image/default.jpg"));
    }

    @Test
    @DisplayName("예약 가능한 상태에서 대기하는 경우 예외가 발생한다.")
    void validateCanMakeWaitingReservation_test() {
        // given
        Member member = em.find(Member.class, 1L);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        Map<String, Object> request = new HashMap<>();
        LocalDate date = LocalDate.now().plusDays(2);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 예약 가능한 상태에서는 대기할 수 없습니다.", "/waiting-reservations");
        request.put("date", date.toString());
        request.put("time", 1);
        request.put("theme", 2);
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtToken)
                .body(request)
                .when().post("/waiting-reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 예약한 상태에서 대기하는 경우 예외가 발생한다.")
    void validateCanMakeWaitingReservation_already_reserved_test() {
        // given
        Member member = em.find(Member.class, 1L);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        Map<String, Object> request = new HashMap<>();
        LocalDate date = LocalDate.now().plusDays(1);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 이미 예약을 완료하였습니다.", "/waiting-reservations");
        request.put("date", date.toString());
        request.put("time", 2);
        request.put("theme", 1);
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtToken)
                .body(request)
                .when().post("/waiting-reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 예약한 상태에서 대기하는 경우 예외가 발생한다.")
    void validateCanMakeWaitingReservation_already_waited_test() {
        // given
        Member member = em.find(Member.class, 2L);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        Map<String, Object> request = new HashMap<>();
        LocalDate date = LocalDate.now().plusDays(1);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 이미 예약 대기를 신청했습니다.", "/waiting-reservations");
        request.put("date", date.toString());
        request.put("time", 2);
        request.put("theme", 1);
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtToken)
                .body(request)
                .when().post("/waiting-reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("본인의 전체 예약 내역을 가져온다.")
    void getMyReservations_test() {
        // given
        Member member = em.find(Member.class, 2L);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        LocalDate date = LocalDate.now().plusDays(1);
        // when & then
        RestAssured.given().log().all()
                .cookie("token", jwtToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("[0].reservationId", equalTo(2))
                .body("[0].theme", equalTo("테마2"))
                .body("[0].date", equalTo(date.toString()))
                .body("[0].time", equalTo("10:00:00"))
                .body("[0].status", equalTo("예약"))
                .body("[1].reservationId", equalTo(3))
                .body("[1].theme", equalTo("테마1"))
                .body("[1].date", equalTo(date.toString()))
                .body("[1].time", equalTo("11:00:00"))
                .body("[1].status", equalTo("1번째 예약대기"));
    }

}
