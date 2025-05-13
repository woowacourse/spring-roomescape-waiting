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
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.infrastructure.JwtTokenProvider;

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
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void 예약_전체_조회() {
        Member admin = memberRepository.save(
                new Member(null, "admin", "admin@domain.com", "admin", Role.ADMIN)
        );
        Member member = memberRepository.save(
                new Member(null, "member1", "member1@domain.com", "password1", Role.MEMBER)
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("name", "desc", "thumb"));
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        reservationRepository.save(Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 1), reservationTime, theme
        ));
        reservationRepository.save(Reservation.createWithoutId(
                member, LocalDate.of(2025, 1, 2), reservationTime, theme
        ));
        String token = tokenProvider.createToken(admin.getId().toString(), admin.getRole());

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("api/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void 관리자_예약_추가_성공() {
        Member admin = memberRepository.save(
                new Member(null, "admin", "admin@domain.com", "password1", Role.ADMIN)
        );
        Member member = memberRepository.save(
                new Member(null, "member1", "member1@domain.com", "password1", Role.MEMBER)
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("name", "desc", "thumb"));
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        String token = tokenProvider.createToken(admin.getId().toString(), admin.getRole());

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-08-05");
        reservation.put("memberId", member.getId());
        reservation.put("timeId", theme.getId());
        reservation.put("themeId", reservationTime.getId());

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
                new Member(null, "member1", "member1@domain.com", "password1", Role.MEMBER)
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("name", "desc", "thumb"));
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        String token = tokenProvider.createToken(member.getId().toString(), member.getRole());

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-08-05");
        reservation.put("memberId", member.getId());
        reservation.put("timeId", theme.getId());
        reservation.put("themeId", reservationTime.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .cookie("token", token)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .statusCode(403);
    }
}
