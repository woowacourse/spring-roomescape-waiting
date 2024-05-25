package roomescape.controller.api;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.controller.BaseControllerTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.request.ReservationRequest;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

class ReservationWaitingControllerTest extends BaseControllerTest {

    private static final String RESERVATION_DATE = "2024-05-24";

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.ten());
        Theme theme = themeRepository.save(ThemeFixture.theme());
        Member member = memberRepository.save(MemberFixture.user());
        reservationRepository.save(ReservationFixture.create(RESERVATION_DATE, member, time, theme));
        userLogin();
    }

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void addReservationWaiting() {
        ReservationRequest request = new ReservationRequest(LocalDate.parse(RESERVATION_DATE), 1L, 1L);

        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", token)
                .body(request)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);
    }
}
