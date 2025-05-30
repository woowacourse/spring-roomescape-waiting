package roomescape.waiting.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.repository.MemberRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.ScheduleRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.dto.request.WaitingCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("Test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WaitingControllerTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AuthorizationProvider authorizationProvider;

    private Theme theme;
    private ReservationTime time;
    private Member member;
    private AuthorizationPrincipal principal;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(new Theme(null, "test theme", "test description", "test thumbnail"));
        time = reservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusHours(1)));
        member = memberRepository.save(MemberFixture.createMember(MemberRole.USER));
        scheduleRepository.save(new Schedule(null, LocalDate.now().plusDays(1), time, theme));
        principal = authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(member));
    }

    @Test
    void 예약_대기_생성_확인() {
        // given
        WaitingCreateRequest waitingCreateRequest = new WaitingCreateRequest(LocalDate.now().plusDays(1), 1L, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(waitingCreateRequest)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo(1));
    }

    @Test
    void 예약_대기_ID로_대기_삭제() {
        // given
        WaitingCreateRequest waitingCreateRequest = new WaitingCreateRequest(LocalDate.now().plusDays(1), 1L, 1L);

        // when & then
        int waitingId = RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(waitingCreateRequest)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .when().delete(String.format("/waiting/%s", waitingId))
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

}
