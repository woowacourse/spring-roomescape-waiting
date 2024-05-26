package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
import roomescape.member.domain.MemberRole;
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
class AdminReservationControllerTest {
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
    private Member member = new Member(1L, "관리자", "aa@email.com", "1111", MemberRole.ADMIN);
    private Theme theme = new Theme("Harry Potter", "해리포터와 도비", "thumbnail.jpg");
    private Time time = new Time(LocalTime.of(12, 0));
    private ReservationDetail reservationDetail = new ReservationDetail(theme, time, LocalDate.MAX.minusDays(1));
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
    @DisplayName("성공 : 세부 검색으로 예약 정보를 조회할 수 있다.")
    void findReservationsInCondition() {
        reservation = reservationRepository.save(reservation);

        int actualSize = RestAssured.given()
                .cookie("token", cookie)
                .when()
                .get("/admin/reservations/search?themeId=" + theme.getId() +
                     "&memberId=" + member.getId() +
                     "&dateFrom=" + LocalDate.MIN +
                     "&dateTo=" + LocalDate.MAX)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getInt("size()");

        assertThat(actualSize).isEqualTo(1);
    }

    @Test
    @DisplayName("성공 : 예약을 만들 수 있다.")
    void createReservation() {
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
                .post("/admin/reservations")
                .then()
                .statusCode(201)
                .header("Location", "/admin/reservations/1");

        List<Reservation> actual = reservationRepository.findAll();
        assertThat(actual).hasSize(1);
    }
}
