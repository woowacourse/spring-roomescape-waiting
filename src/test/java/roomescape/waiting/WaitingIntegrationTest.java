package roomescape.waiting;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.util.IntegrationTest;
import roomescape.waiting.dto.request.CreateWaitingRequest;
import roomescape.waiting.dto.response.FindWaitingWithRankingResponse;
import roomescape.waiting.model.Waiting;
import roomescape.waiting.model.WaitingWithRanking;
import roomescape.waiting.repository.WaitingRepository;

@IntegrationTest
class WaitingIntegrationTest {

    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    @Autowired
    WaitingIntegrationTest(final MemberRepository memberRepository,
                           final ReservationTimeRepository reservationTimeRepository,
                           final ThemeRepository themeRepository, final ReservationRepository reservationRepository,
                           final WaitingRepository waitingRepository) {
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin(Member member) {
        return RestAssured
                .given().log().all()
                .body(new LoginRequest(member.getEmail().getEmail(), member.getPassword()))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @DisplayName("방탈출 예약 대기 성공")
    @Test
    void createReservationWaiting() {
        LocalDate date = LocalDate.parse("2024-11-30");
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        reservationRepository.save(new Reservation(member, date, reservationTime, theme));

        CreateWaitingRequest createWaitingRequest = new CreateWaitingRequest(date, 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(member))
                .body(createWaitingRequest)
                .when().post("/waitings")
                .then().log().all()

                .statusCode(201)
                .body("waitingId", equalTo(1))
                .body("reservationId", equalTo(1))
                .body("memberId", equalTo(1));
    }

    @DisplayName("방탈출 예약 대기 실패: 예약 없음")
    @Test
    void createReservationWaiting_WhenReservationNotExists() {
        LocalDate date = LocalDate.parse("2024-11-30");
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        CreateWaitingRequest createWaitingRequest = new CreateWaitingRequest(date, 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(member))
                .body(createWaitingRequest)
                .when().post("/waitings")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("2024-11-30의 time: 1, theme: 1의 예약이 존재하지 않습니다."));
    }

    @DisplayName("방탈출 예약 대기 실패: 중복 대기")
    @Test
    void createReservationWaiting_WhenMemberNotExistsReservationsAndMember() {
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-11-30"), reservationTime, theme));

        waitingRepository.save(new Waiting(reservation, member));
        CreateWaitingRequest createWaitingRequest = new CreateWaitingRequest(reservation.getDate(), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(member))
                .body(createWaitingRequest)
                .when().post("/waitings")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("memberId: 1 회원이 reservationId: 1인 예약에 대해 이미 대기를 신청했습니다."));
    }

    @DisplayName("예약 대기 정보 목록 조회")
    @Test
    void getWaitings() {
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        Member otherMember = memberRepository.save(MemberFixture.getOne("otherMember@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-11-30"), reservationTime, theme));
        Reservation otherReservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2025-01-30"), reservationTime, theme));

        Waiting waiting1 = waitingRepository.save(new Waiting(reservation, otherMember));
        Waiting waiting2 = waitingRepository.save(new Waiting(otherReservation, member));
        Waiting waiting3 = waitingRepository.save(new Waiting(reservation, member));

        List<FindWaitingWithRankingResponse> findWaitingWithRankingRespons = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(member))
                .when().get("/members/waitings")
                .then().log().all()

                .statusCode(200).extract().jsonPath()
                .getList(".", FindWaitingWithRankingResponse.class);
        assertTrue(findWaitingWithRankingRespons.contains(
                FindWaitingWithRankingResponse.of(new WaitingWithRanking(waiting2, 0L))));
        assertTrue(findWaitingWithRankingRespons.contains(
                FindWaitingWithRankingResponse.of(new WaitingWithRanking(waiting3, 1L))));
    }

    @DisplayName("방탈출 예약 대기 삭제 성공")
    @Test
    void deleteWaiting() {
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-11-30"), reservationTime, theme));

        waitingRepository.save(new Waiting(reservation, member));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(member))
                .when().delete("/waitings/1")
                .then().log().all()

                .statusCode(204);
    }

    @DisplayName("예약 대기 삭제 실패: 예약 대기 없음")
    @Test
    void deleteWaiting_WhenWaitingNotExists() {
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-30"), reservationTime, theme));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(member))
                .when().delete("/waitings/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 예약 대기가 존재하지 않습니다."));
    }

    @DisplayName("예약 대기 삭제 실패: 회원의 권한이 없는 경우")
    @Test
    void deleteWaiting_WhenMember() {
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        Member forbiddenMember = memberRepository.save(MemberFixture.getOne("forbiddenMember@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-11-30"), reservationTime, theme));

        waitingRepository.save(new Waiting(reservation, member));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(forbiddenMember))
                .when().delete("/waitings/1")
                .then().log().all()

                .statusCode(403)
                .body("detail", equalTo("회원의 권한이 없어, 식별자 2인 예약 대기를 삭제할 수 없습니다."));
    }

    @DisplayName("예약 대기 거절 성공")
    @Test
    void rejectWaiting() {
        Member admin = memberRepository.save(MemberFixture.getAdmin());
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(admin, LocalDate.parse("2024-11-30"), reservationTime, theme));

        waitingRepository.save(new Waiting(reservation, admin));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(admin))
                .when().delete("/admin/waitings/reject/1")
                .then().log().all()

                .statusCode(204);
    }

    @DisplayName("예약 대기 거절 실패: 대기 없음")
    @Test
    void rejectWaiting_WhenWaitingNotExist_throwException() {
        Member admin = memberRepository.save(MemberFixture.getAdmin());
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(admin, LocalDate.parse("2024-11-30"), reservationTime, theme));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(admin))
                .when().delete("/admin/waitings/reject/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 예약 대기가 존재하지 않습니다."));
    }
}
