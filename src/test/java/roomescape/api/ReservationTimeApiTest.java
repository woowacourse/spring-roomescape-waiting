package roomescape.api;

import static org.hamcrest.Matchers.is;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.dto.business.AccessTokenContent;
import roomescape.dto.request.ReservationTimeCreationRequest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.utility.JwtTokenProvider;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Rollback(value = false)
class ReservationTimeApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        memberRepository.deleteAll();
        timeRepository.deleteAll();
        themeRepository.deleteAll();
    }

    @DisplayName("모든 예약 시간을 조회할 수 있다.")
    @Test
    void canFindAllReservationTimes() {
        // given
        timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(11, 0)));
        timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(12, 0)));

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(3));
    }

    @DisplayName("에약 여부와 함께 모든 예약 시간을 조회할 수 있다.")
    @Test
    void canFindReservationTimesWithBookState() {
        // given
        Member member = memberRepository.save(
                Member.createWithoutId(Role.GENERAL, "회원", "member@email.com", "qwer1234!"));
        Theme theme = themeRepository.save(
                Theme.createWithoutId("테마", "설명", "섬네일"));

        ReservationTime bookedTime = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationTime notBookedTime = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(11, 0)));

        reservationRepository.save(Reservation.createWithoutId(NEXT_DAY, bookedTime, theme, member));

        Map<String, Object> params = new HashMap<>();
        params.put("themeId", theme.getId());
        params.put("date", NEXT_DAY.toString());

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .queryParams(params)
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(2))
                .body("get(0).get(\"alreadyBooked\")", is(true))
                .body("get(1).get(\"alreadyBooked\")", is(false));
    }

    @DisplayName("예약 시간을 추가할 수 있다.")
    @Test
    void canAddReservationTime() {
        // given
        Member admin = memberRepository.save(
                Member.createWithoutId(Role.ADMIN, "관리자", "admin@email.com", "qwer1234!"));

        AccessTokenContent tokenContent = new AccessTokenContent(admin.getId(), admin.getRole(), admin.getName());
        String accessToken = tokenProvider.createAccessToken(tokenContent);

        ReservationTimeCreationRequest creationContent = new ReservationTimeCreationRequest(LocalTime.of(10, 0));

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .cookie("access", accessToken)
                .body(creationContent)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("ID를 통해 예약 시간을 삭제할 수 있다.")
    @Test
    void canDeleteReservationTime() {
        // given
        Member admin = memberRepository.save(
                Member.createWithoutId(Role.ADMIN, "관리자", "admin@email.com", "qwer1234!"));
        ReservationTime time = timeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));

        AccessTokenContent tokenContent = new AccessTokenContent(admin.getId(), admin.getRole(), admin.getName());
        String accessToken = tokenProvider.createAccessToken(tokenContent);

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .cookie("access", accessToken)
                .when().delete("/times/" + time.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
