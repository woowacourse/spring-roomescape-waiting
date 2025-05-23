package roomescape.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;
import roomescape.reservation.infrastructure.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminApiTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    void 예약_전체_조회() {
        Member admin = memberRepository.save(
                Member.builder()
                        .name("admin")
                        .email("admin@domain.com")
                        .password("password1")
                        .role(Role.ADMIN).build()
        );
        Member member = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("name")
                        .description("desc")
                        .thumbnail("thumb").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build());
        reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(timeSlot)
                        .theme(theme).build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 2))
                        .timeSlot(timeSlot)
                        .theme(theme).build()
        );
        String token = tokenProvider.createToken(admin.getId().toString(), admin.getRole());

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("api/admin/reservations?memberId=2&themeId=1&dateFrom=2025-01-01&dateTo=2025-01-02")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void 관리자_예약_추가_성공() {
        Member admin = memberRepository.save(
                Member.builder()
                        .name("admin")
                        .email("admin@domain.com")
                        .password("password1")
                        .role(Role.ADMIN).build()
        );
        Member member = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("theme")
                        .description("desc")
                        .thumbnail("thumb").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build());
        String token = tokenProvider.createToken(admin.getId().toString(), admin.getRole());

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-08-05");
        reservation.put("memberId", member.getId());
        reservation.put("timeId", theme.getId());
        reservation.put("themeId", timeSlot.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .cookie("token", token)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("date", equalTo("2026-08-05"))
                .body("memberName", equalTo("member1"));
    }

    @Test
    void 관리자가_아닌_사용자가_관리자_예약_추가할_경우_403에러가_발생한다() {
        Member member = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("name")
                        .description("desc")
                        .thumbnail("thumb").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build());
        String token = tokenProvider.createToken(member.getId().toString(), member.getRole());

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-08-05");
        reservation.put("memberId", member.getId());
        reservation.put("timeId", theme.getId());
        reservation.put("themeId", timeSlot.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .cookie("token", token)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 관리자가_모든_대기를_조회한다() {
        Member admin = memberRepository.save(
                Member.builder()
                        .name("admin")
                        .email("admin@domain.com")
                        .password("admin")
                        .role(Role.ADMIN).build()
        );
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("name")
                        .description("desc")
                        .thumbnail("thumb").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build());
        waitingRepository.save(
                Waiting.builder()
                        .date(LocalDate.of(2025, 1, 1))
                        .theme(theme)
                        .timeSlot(timeSlot)
                        .member(admin).build()
        );
        String token = tokenProvider.createToken(admin.getId().toString(), admin.getRole());
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/api/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 관리자가_대기를_승인한다() {
        // given
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.ADMIN).build()
        );
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .thumbnail("thumbnail1")
                        .description("description1").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build()
        );
        Waiting waiting = waitingRepository.save(
                Waiting.builder()
                        .theme(theme)
                        .member(member1)
                        .timeSlot(timeSlot)
                        .date(LocalDate.now().plusDays(1)).build()
        );
        String token = tokenProvider.createToken("1", Role.ADMIN);
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().post("/api/admin/waitings/{waitingId}", waiting.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 관리자가_대기를_거절한다() {
        // given
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.ADMIN).build()
        );
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .thumbnail("thumbnail1")
                        .description("description1").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build()
        );
        Waiting waiting = waitingRepository.save(
                Waiting.builder()
                        .theme(theme)
                        .member(member1)
                        .timeSlot(timeSlot)
                        .date(LocalDate.now().plusDays(1)).build()
        );
        String token = tokenProvider.createToken("1", Role.ADMIN);
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().post("/api/admin/waitings/{waitingId}", waiting.getId())
                .then().log().all()
                .statusCode(204);
    }
}
