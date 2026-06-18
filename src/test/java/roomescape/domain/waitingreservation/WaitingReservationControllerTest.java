package roomescape.domain.waitingreservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class WaitingReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    private Long date1Id;
    private Long time1Id;
    private Long time2Id;
    private Long theme1Id;
    private Long theme4Id;
    private Long firstWaitingId;
    private Long goraeMemberId;
    private Long existingWaiterMemberId;

    @BeforeEach
    void setUp() {
        RestAssured.port = this.port;

        ReservationDate date1 = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(10)));
        ReservationDate date2 = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(15)));
        ReservationDate date3 = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(20)));
        date1Id = date1.getId();

        ReservationTime time1 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(22, 0)));
        ReservationTime time2 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(12, 0)));
        ReservationTime time3 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(14, 0)));
        time1Id = time1.getId();
        time2Id = time2.getId();

        Theme theme1 = themeRepository.save(Theme.createWithoutId("테스트테마", "설명", "url"));
        Theme theme2 = themeRepository.save(Theme.createWithoutId("공포테마", "무서운 테마", "url2"));
        Theme theme3 = themeRepository.save(Theme.createWithoutId("스릴러테마", "스릴 넘치는 테마", "url3"));
        Theme theme4 = themeRepository.save(Theme.createWithoutId("코미디테마", "웃긴 테마", "url4"));
        theme1Id = theme1.getId();
        theme4Id = theme4.getId();

        Member existingReserver = memberRepository.save(Member.createWithoutId("기존예약자"));
        Member existingWaiter = memberRepository.save(Member.createWithoutId("기존대기자"));
        Member gorae = memberRepository.save(Member.createWithoutId("고래"));
        Member namu = memberRepository.save(Member.createWithoutId("나무"));
        Member isan = memberRepository.save(Member.createWithoutId("이산"));
        goraeMemberId = gorae.getId();
        existingWaiterMemberId = existingWaiter.getId();

        reservationRepository.save(Reservation.createWithoutId(existingReserver, date1, time1, theme1));
        reservationRepository.save(Reservation.createWithoutId(existingReserver, date2, time2, theme2));
        reservationRepository.save(Reservation.createWithoutId(existingReserver, date3, time3, theme3));
        reservationRepository.save(Reservation.createWithoutId(existingReserver, date1, time1, theme4));

        WaitingReservation firstWaiting = waitingReservationRepository.save(
            WaitingReservation.createWithoutId(existingWaiter, date1, time1, theme1, LocalDateTime.now().minusHours(2)));
        firstWaitingId = firstWaiting.getId();
        waitingReservationRepository.save(WaitingReservation.createWithoutId(gorae, date1, time1, theme1, LocalDateTime.now().minusHours(1)));
        waitingReservationRepository.save(WaitingReservation.createWithoutId(namu, date2, time2, theme2, LocalDateTime.now().minusHours(3)));
        waitingReservationRepository.save(WaitingReservation.createWithoutId(isan, date2, time2, theme2, LocalDateTime.now().minusHours(2)));
        waitingReservationRepository.save(WaitingReservation.createWithoutId(gorae, date2, time2, theme2, LocalDateTime.now().minusHours(1)));
        waitingReservationRepository.save(WaitingReservation.createWithoutId(gorae, date3, time3, theme3, LocalDateTime.now()));
    }

    @Test
    void 사용자는_예약_대기를_신청한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", goraeMemberId);
        params.put("dateId", date1Id);
        params.put("timeId", time1Id);
        params.put("themeId", theme4Id);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when()
            .post("/waiting-reservations")
            .then().log().all()
            .statusCode(201)
            .body("name", is("고래"))
            .body("theme.name", is("코미디테마"));
    }

    @Test
    void 중복_예약_대기_신청을_하면_409를_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", existingWaiterMemberId);
        params.put("dateId", date1Id);
        params.put("timeId", time1Id);
        params.put("themeId", theme1Id);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when()
            .post("/waiting-reservations")
            .then().log().all()
            .statusCode(409);
    }

    @Test
    void 예약_가능한_시간에_대기_신청하면_409을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", goraeMemberId);
        params.put("dateId", date1Id);
        params.put("timeId", time2Id);
        params.put("themeId", theme1Id);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when()
            .post("/waiting-reservations")
            .then().log().all()
            .statusCode(409);
    }

    @Test
    void 존재하지_않는_슬롯에_대기_신청을_하면_404을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", goraeMemberId);
        params.put("dateId", 999);
        params.put("timeId", 999);
        params.put("themeId", 999);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when()
            .post("/waiting-reservations")
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void 사용자는_예약_대기를_취소한다() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .when()
            .delete("/waiting-reservations/" + firstWaitingId)
            .then().log().all()
            .statusCode(204);
    }

    @Test
    void 예약_대기_목록과_순번을_조회한다() {
        RestAssured.given().log().all()
            .param("memberId", goraeMemberId)
            .when().get("/waiting-reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(3))
            .body("find {it.theme.name == '테스트테마'}.rank", is(2))
            .body("find {it.theme.name == '공포테마'}.rank", is(3))
            .body("find {it.theme.name == '스릴러테마'}.rank", is(1));
    }
}
