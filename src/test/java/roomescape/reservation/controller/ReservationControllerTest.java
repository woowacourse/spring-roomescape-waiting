package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationWaitingRepository waitingRepository;
    @Autowired
    private ReservationDetailRepository detailRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeRepository timeRepository;

    private String cookie;
    private Member member = new Member("범블비", "aa@email.com", "1111");
    private Theme theme = new Theme("Harry Potter", "해리포터와 도비", "thumbnail.jpg");
    private Time time = new Time(LocalTime.of(12, 0));
    private ReservationDetail reservationDetail = new ReservationDetail(theme, time, LocalDate.MAX);
    private Reservation reservation = new Reservation(member, reservationDetail);
    private ReservationWaiting waiting = new ReservationWaiting(member, reservationDetail);

    @BeforeEach
    void login() {
        member = memberRepository.save(member);
        theme = themeRepository.save(theme);
        time = timeRepository.save(time);
        reservationDetail = detailRepository.save(reservationDetail);

        RestAssured.port = port;

        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(member.getEmail(), member.getPassword());

        cookie = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberLoginRequest)
                .when().post("/login")
                .then()
                .statusCode(200)
                .log().all().extract()
                .cookie("token");
    }

    @Test
    @DisplayName("성공 : 예약 및 예약 대기 정보를 얻을 수 있다.")
    void findReservations() {
        reservation = reservationRepository.save(reservation);
        waiting = waitingRepository.save(waiting);

        int actualSize = RestAssured.given()
                .cookie("token", cookie)
                .when()
                .get("/reservations")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getInt("size()");

        assertThat(actualSize).isEqualTo(2);
    }

    @Test
    @DisplayName("성공 : 회원의 예약 및 예약 대기만 조회할 수 있다.")
    void findReservationsByMember() {
        reservation = reservationRepository.save(reservation);
        waiting = waitingRepository.save(waiting);

        Member otherMember = new Member("켬미", "aa@naver.com", "1234");
        memberRepository.save(otherMember);
        waitingRepository.save(new ReservationWaiting(otherMember, reservationDetail)); // 해당 예약은 내 예약이 아니므로 조회되지 않음
        reservationRepository.save(reservation); // 해당 예약은 내 예약으로 조회

        int actualSize = RestAssured.given()
                .cookie("token", cookie)
                .when()
                .get("/reservations/mine")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getInt("size()");

        assertThat(actualSize).isEqualTo(2);
    }

    @Test
    @DisplayName("성공 : 해당 예약 정보에 예약 가능한 시간을 조회한다.")
    void findReservationTimes() {
        reservation = reservationRepository.save(reservation);

        Time otherTime = new Time(LocalTime.of(20, 0));
        timeRepository.save(otherTime); // 예약되지 않은 시간으로 1개 조회
        reservationRepository.save(reservation); // 예약된 시간 1개는 조회되지 않음

        int actualSize = RestAssured.given()
                .cookie("token", cookie)
                .when()
                .get("/reservations/times/"+theme.getId()+"?date="+reservationDetail.getDate())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getInt("size()");

        assertThat(actualSize).isEqualTo(1);
    }

    @Test
    @DisplayName("성공 : 예약을 만들 수 있다.")
    void createReservation() {
        Map<String, String> params = Map.of(
                "themeId", reservation.getThemeId().toString(),
                "timeId", reservation.getTimeId().toString(),
                "date", reservation.getDate().toString()
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", cookie)
                .body(params)
                .when()
                .post("/reservations")
                .then()
                .statusCode(201)
                .header("Location", "/reservations/1");

        List<Reservation> actual = reservationRepository.findAll();
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("성공 : 예약 대기를 만들 수 있다.")
    void createWaitingReservation() {
        ReservationCreateRequest params = new ReservationCreateRequest(
                reservation.getMemberId(),
                reservation.getThemeId(),
                reservation.getTimeId(),
                reservation.getDate()
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", cookie)
                .body(params)
                .when()
                .post("/reservations/waiting")
                .then()
                .statusCode(201)
                .header("Location", "/reservations/waiting/1");

        List<ReservationWaiting> actual = waitingRepository.findAll();
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("성공 : 예약을 제거할 수 있다.")
    void deleteReservation() {
        reservationRepository.save(reservation);

        RestAssured.given()
                .when()
                .delete("/reservations/1")
                .then()
                .statusCode(204);

        Optional<Reservation> expected = reservationRepository.findById(1L);
        assertThat(expected).isEmpty();
    }

    @Test
    @DisplayName("성공 : 예약 대기를 제거할 수 있다.")
    void deleteReservationWaiting() {
        waitingRepository.save(waiting);

        RestAssured.given()
                .when()
                .delete("/reservations/waiting/1")
                .then()
                .statusCode(204);

        Optional<ReservationWaiting> expected = waitingRepository.findById(1L);
        assertThat(expected).isEmpty();
    }
}
