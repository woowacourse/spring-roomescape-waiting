package roomescape.domain.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ReservationDate futureDate;
    private ReservationDate pastDate;
    private ReservationDate todayDate;
    private ReservationTime time;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        futureDate = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(10)));
        pastDate = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().minusDays(1)));
        todayDate = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now()));
        time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(22, 0)));
        theme = themeRepository.save(Theme.createWithoutId("테스트테마", "설명", "url"));
        member = memberRepository.save(Member.createWithoutId("테스터"));
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", member.getId());
        params.put("dateId", futureDate.getId());
        params.put("timeId", time.getId());
        params.put("themeId", theme.getId());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("name", is("테스터"));
    }

    @Test
    @DisplayName("과거 시간으로 예약을 생성할 수 없다.")
    void createReservation_Fail_PastTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", member.getId());
        params.put("dateId", pastDate.getId());
        params.put("timeId", time.getId());
        params.put("themeId", theme.getId());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("중복된 예약은 생성할 수 없다.")
    void createReservation_Fail_Duplicated() {
        Member existing = memberRepository.save(Member.createWithoutId("기존테스터"));
        reservationRepository.save(Reservation.createWithoutId(existing, futureDate, time, theme));

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", member.getId());
        params.put("dateId", futureDate.getId());
        params.put("timeId", time.getId());
        params.put("themeId", theme.getId());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(409);
    }

    @Test
    @DisplayName("멤버 ID로 예약을 조회한다.")
    void getReservationsByMemberId() {
        reservationRepository.save(Reservation.createWithoutId(member, futureDate, time, theme));

        RestAssured.given().log().all()
            .param("memberId", member.getId())
            .when().get("/reservations-mine")
            .then().log().all()
            .statusCode(200)
            .body("any { it.name == '테스터' }", is(true));
    }

    @Test
    @DisplayName("예약을 취소(삭제)한다.")
    void cancelReservation() {
        Reservation saved = reservationRepository.save(Reservation.createWithoutId(member, futureDate, time, theme));

        RestAssured.given().log().all()
            .when().delete("/reservations/" + saved.getId())
            .then().log().all()
            .statusCode(204);
    }

    @Test
    @DisplayName("당일 예약은 취소(삭제)할 수 없다.")
    void cancelReservation_Fail_Today() {
        Reservation saved = reservationRepository.save(Reservation.createWithoutId(member, todayDate, time, theme));

        RestAssured.given().log().all()
            .when().delete("/reservations/" + saved.getId())
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("예약을 수정한다.")
    void updateReservation() {
        Reservation saved = reservationRepository.save(Reservation.createWithoutId(member, futureDate, time, theme));
        ReservationDate newDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.now().plusDays(15)));

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", newDate.getId());
        params.put("timeId", time.getId());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + saved.getId())
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("당일 예약은 수정할 수 없다.")
    void updateReservation_Fail_Today() {
        Reservation saved = reservationRepository.save(Reservation.createWithoutId(member, todayDate, time, theme));

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", futureDate.getId());
        params.put("timeId", time.getId());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + saved.getId())
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("과거 시간으로 예약을 수정할 수 없다.")
    void updateReservation_Fail_PastTime() {
        Reservation saved = reservationRepository.save(Reservation.createWithoutId(member, futureDate, time, theme));

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", pastDate.getId());
        params.put("timeId", time.getId());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + saved.getId())
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("수정하려는 시간에 이미 다른 예약이 있으면 수정할 수 없다.")
    void updateReservation_Fail_Duplicated() {
        Reservation myReservation = reservationRepository.save(
            Reservation.createWithoutId(member, futureDate, time, theme));

        Member other = memberRepository.save(Member.createWithoutId("다른사람"));
        ReservationDate otherDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.now().plusDays(15)));
        reservationRepository.save(Reservation.createWithoutId(other, otherDate, time, theme));

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", otherDate.getId());
        params.put("timeId", time.getId());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + myReservation.getId())
            .then().log().all()
            .statusCode(409);
    }
}
