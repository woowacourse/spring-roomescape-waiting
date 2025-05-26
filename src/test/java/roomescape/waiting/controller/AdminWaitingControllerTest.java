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
public class AdminWaitingControllerTest {

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

    private AuthorizationPrincipal principal;

    private AuthorizationPrincipal adminPrincipal;

    private Member admin;

    private int waitingId;

    @BeforeEach
    void setUp() {
        Theme theme = themeRepository.save(new Theme(null, "test theme", "test description", "test thumbnail"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusHours(1)));
        Member member = memberRepository.save(MemberFixture.createMember(MemberRole.USER));
        admin = memberRepository.save(MemberFixture.createMember(MemberRole.ADMIN));
        scheduleRepository.save(new Schedule(null, LocalDate.now().plusDays(1), time, theme));
        principal = authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(member));
        adminPrincipal = authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(admin));

        WaitingCreateRequest waitingCreateRequest = new WaitingCreateRequest(LocalDate.now().plusDays(1), 1L, 1L);

        waitingId = RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(waitingCreateRequest)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

    }

    @Test
    void 관리자_예약_대기_목록_불러오기() {
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", adminPrincipal.value())
                .when().get("/admin/waiting")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1));
    }

    @Test
    void 관리자_예약_대기_승인() {
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", adminPrincipal.value())
                .when().post(String.format("/admin/waiting/%s", waitingId))
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 관리자_예약_대기_거절() {
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", adminPrincipal.value())
                .when().delete(String.format("/admin/waiting/%s", waitingId))
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }


}
