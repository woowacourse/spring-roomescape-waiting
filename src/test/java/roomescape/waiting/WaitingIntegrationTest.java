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
}
