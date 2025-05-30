package roomescape.reservation.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.ScheduleRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AdminReservationControllerTest {

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

    @BeforeEach
    void setUp() {
        Theme theme = new Theme(null, "test theme", "test description", "test thumbnail");
        themeRepository.save(theme);

        ReservationTime reservationTime = new ReservationTime(null, LocalTime.now().plusHours(1));
        reservationTimeRepository.save(reservationTime);

        Schedule schedule = new Schedule(null, LocalDate.now().plusDays(1), reservationTime, theme);
        scheduleRepository.save(schedule);

        Member member = MemberFixture.createMember(MemberRole.ADMIN);
        Member savedMember = memberRepository.save(member);

        principal = authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(savedMember));
    }

    @Test
    void 관리자_예약_생성_확인() {
        // given
        AdminReservationCreateRequest request = new AdminReservationCreateRequest(LocalDate.now(), 1L, 1L, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo(1));
    }

    @Test
    void 관리자_전체_예약_목록_불러오기() {
        // given
        createReservation();
        ReservationSearchConditionRequest request = new ReservationSearchConditionRequest(
                1L, 1L, LocalDate.now(), LocalDate.now().plusDays(1)
        );

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(request)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1));
    }


    private void createReservation() {
        // given
        AdminReservationCreateRequest request = new AdminReservationCreateRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(request)
                .when().post("/admin/reservations");
    }
}
