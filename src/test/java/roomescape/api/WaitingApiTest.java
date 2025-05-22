package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;
import roomescape.reservation.infrastructure.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingApiTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void 대기를_생성한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
        );
        Member member2 = memberRepository.save(
                Member.builder()
                        .name("member2")
                        .password("password2")
                        .email("email2@domain.com")
                        .role(Role.MEMBER).build()
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
        reservationRepository.save(
                Reservation.builder()
                        .theme(theme)
                        .member(member2)
                        .timeSlot(timeSlot)
                        .date(date).build()
        );
        String token = tokenProvider.createToken("1", Role.MEMBER);
        Map<String, Object> body = Map.of(
                "themeId", 1,
                "timeId", 1,
                "date", date.toString()
        );
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(body)
                .when().post("/api/waiting")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);
    }

    @Test
    void 사용자의_대기목록을_조회한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
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
        waitingRepository.save(
                Waiting.builder()
                        .theme(theme)
                        .member(member1)
                        .timeSlot(timeSlot)
                        .date(date).build()
        );
        String token = tokenProvider.createToken("1", Role.MEMBER);
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/api/waiting/my")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 대기를_삭제한다() {
        // given
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
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
        String token = tokenProvider.createToken("1", Role.MEMBER);
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/api/waiting/{waitingId}", waiting.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 삭제할_대기가_존재하지_않는_경우_NotFound_에러가_발생한다() {
        // given
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
        );
        String token = tokenProvider.createToken(member1.getId().toString(), Role.MEMBER);
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/api/waiting/{waitingId}", 1000000L)
                .then().log().all()
                .statusCode(404);
    }
}
