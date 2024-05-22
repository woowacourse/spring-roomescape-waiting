package roomescape.waiting;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.waiting.model.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@IntegrationTest
class WaitingIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

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

    @DisplayName("방탈출 예약 대기 성공 시, 생성된 대기에 대한 정보를 반환한다.")
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

    @DisplayName("방탈출 예약 대기 요청 시, 예약이 존재하지 않는다면 예외를 반환한다.")
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
                .body("detail", equalTo("2024-11-30의 timeId: 1, themeId: 1의 예약이 존재하지 않습니다."));
    }

    @DisplayName("방탈출 예약 대기 요청 시 이미 회원에 대한 대기가 존재할 경우, 예외를 반환한다.")
    @Test
    void createReservationWaiting_WhenMemberNotExistsReservationsAndMember() {
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-30"), reservationTime, theme));

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

    @DisplayName("방탈출 예약 대기를 삭제한다.")
    @Test
    void deleteWaiting() {
        Member member = memberRepository.save(MemberFixture.getOne("asdf12@navv.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-30"), reservationTime, theme));

        waitingRepository.save(new Waiting(reservation, member));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(member))
                .when().delete("/waitings/1")
                .then().log().all()

                .statusCode(204);
    }

    @DisplayName("방탈출 예약 대기 삭제 시, 예약 대기가 없는 경우 예외를 반환한다.")
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

    @DisplayName("방탈출 예약 대기 삭제 시, 회원의 권한이 없는 경우 예외를 반환한다.")
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

}
