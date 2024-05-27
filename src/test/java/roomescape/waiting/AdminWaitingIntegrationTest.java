package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;

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
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.util.IntegrationTest;
import roomescape.waiting.dto.response.FindWaitingResponse;
import roomescape.waiting.model.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@IntegrationTest
class AdminWaitingIntegrationTest {

    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    AdminWaitingIntegrationTest(final MemberRepository memberRepository,
                                final ReservationTimeRepository reservationTimeRepository,
                                final ThemeRepository themeRepository, final WaitingRepository waitingRepository,
                                final ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin() {
        memberRepository.save(new Member("비밥", Role.ADMIN, "admin@naver.com", "hihi"));
        return RestAssured
                .given().log().all()
                .body(new LoginRequest("admin@naver.com", "hihi"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("방탈출 예약 대기 목록 조회 성공")
    void getWaitings() {
        // given
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation1 = reservationRepository.save(
                new Reservation(memberRepository.getById(1L), LocalDate.parse("2024-11-23"),
                        reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(memberRepository.getById(1L), LocalDate.parse("2024-12-23"),
                        reservationTimeRepository.getById(1L), themeRepository.getById(1L)));

        Waiting waiting1 = waitingRepository.save(new Waiting(reservation1, memberRepository.getById(1L)));
        Waiting waiting2 = waitingRepository.save(new Waiting(reservation2, memberRepository.getById(1L)));
        // when
        List<FindWaitingResponse> findReservationResponses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .when().get("/admin/waitings")
                .then().log().all()

                .statusCode(200)
                .extract().jsonPath()
                .getList(".", FindWaitingResponse.class);

        // then
        assertThat(findReservationResponses).containsExactly(
                FindWaitingResponse.from(waiting1),
                FindWaitingResponse.from(waiting2));
    }
}
