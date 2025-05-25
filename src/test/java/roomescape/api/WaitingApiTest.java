package roomescape.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.infrastructure.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingApiTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void 사용자가_예약대기를_추가한다() {
        Member savedMember = memberRepository.save(
                new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER)
        );

        String token = tokenProvider.createToken(savedMember.getId().toString(), savedMember.getRole());
        ReservationTime time = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("theme1", "desc", "thumb1"));
        Map<String, Object> waiting = new HashMap<>();
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", time.getId());
        waiting.put("themeId", theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .cookie("token", token)
                .when().post("/api/waiting")
                .then().log().all()
                .statusCode(201)
                .body("date", equalTo("2026-08-05"))
                .body("memberName", equalTo("name1"));
    }

    @Test
    void 예약대기를_삭제한다() {
        // given
        Member member = memberRepository.save(
                new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER)
        );
        ReservationTime time = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("theme1", "desc", "thumb1"));
        Waiting waiting = waitingRepository.save(
                Waiting.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme));
        // when & then
        RestAssured.given().log().all()
                .when().delete("/api/waiting/{waitingId}", waiting.getId())
                .then().log().all()
                .statusCode(204);

        assertThat(reservationRepository.findById(waiting.getId())).isEmpty();
    }

    @Test
    void 중복_예약대기는_불가능하다() {
        // given
        Member savedMember = memberRepository.save(
                new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER)
        );
        ReservationTime time = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("theme1", "desc", "thumb1"));
        waitingRepository.save(
                Waiting.createWithoutId(savedMember, LocalDate.of(2025, 1, 1), time, theme));

        String token = tokenProvider.createToken(savedMember.getId().toString(), savedMember.getRole());
        Map<String, Object> waiting = new HashMap<>();
        waiting.put("date", "2025-01-01");
        waiting.put("timeId", time.getId());
        waiting.put("themeId", theme.getId());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .cookie("token", token)
                .when().post("/api/waiting")
                .then().log().all()
                .statusCode(400);
    }
}
