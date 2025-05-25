package roomescape.api;

import static org.hamcrest.Matchers.is;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;
import static roomescape.test.fixture.DateFixture.TODAY;
import static roomescape.test.fixture.DateFixture.YESTERDAY;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
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
import roomescape.dto.request.ThemeCreationRequest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.utility.JwtTokenProvider;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Rollback(value = false)
class ThemeApiTest {

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

    @DisplayName("모든 테마를 조회할 수 있다.")
    @Test
    void canFindAllTheme() {
        // given
        themeRepository.save(Theme.createWithoutId("테마", "설명", "섬네일"));
        themeRepository.save(Theme.createWithoutId("테마", "설명", "섬네일"));
        themeRepository.save(Theme.createWithoutId("테마", "설명", "섬네일"));

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(3));
    }

    @DisplayName("인기순으로 테마를 조회할 수 있다.")
    @Test
    void canFindTopTheme() {
        // given
        Member member = memberRepository.save(
                Member.createWithoutId(Role.GENERAL, "회원", "member@email.com", "qwer1234!"));
        ReservationTime time = timeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));

        Theme firstTheme = themeRepository.save(
                Theme.createWithoutId("테마1", "설명", "섬네일"));
        Theme secendTheme = themeRepository.save(
                Theme.createWithoutId("테마2", "설명", "섬네일"));
        Theme thirdTheme = themeRepository.save(
                Theme.createWithoutId("테마3", "설명", "섬네일"));

        reservationRepository.save(Reservation.createWithoutId(YESTERDAY, time, firstTheme, member));
        reservationRepository.save(Reservation.createWithoutId(TODAY, time, firstTheme, member));
        reservationRepository.save(Reservation.createWithoutId(NEXT_DAY, time, firstTheme, member));

        reservationRepository.save(Reservation.createWithoutId(YESTERDAY, time, secendTheme, member));
        reservationRepository.save(Reservation.createWithoutId(TODAY, time, secendTheme, member));

        reservationRepository.save(Reservation.createWithoutId(YESTERDAY, time, thirdTheme, member));

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .params("size", 3)
                .when().get("/themes/ranking")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("get(0).get(\"name\")", is(firstTheme.getName()))
                .body("get(1).get(\"name\")", is(secendTheme.getName()))
                .body("get(2).get(\"name\")", is(thirdTheme.getName()));
    }

    @DisplayName("테마를 추가할 수 있다.")
    @Test
    void canAddTheme() {
        // given
        Member admin = memberRepository.save(
                Member.createWithoutId(Role.ADMIN, "관리자", "admin@email.com", "qwer1234!"));
        Member member = memberRepository.save(
                Member.createWithoutId(Role.GENERAL, "회원", "member@email.com", "qwer1234!"));
        ReservationTime time = timeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));

        String accessToken = tokenProvider.createAccessToken(
                new AccessTokenContent(admin.getId(), admin.getRole(), admin.getName()));

        ThemeCreationRequest creationContent = new ThemeCreationRequest("테마", "설명", "섬네일");

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .cookie("access", accessToken)
                .body(creationContent)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("ID를 통해 테마를 삭제할 수 있다.")
    @Test
    void canDeleteThemeById() {
        // given
        Member admin = memberRepository.save(
                Member.createWithoutId(Role.ADMIN, "관리자", "admin@email.com", "qwer1234!"));
        Member member = memberRepository.save(
                Member.createWithoutId(Role.GENERAL, "회원", "member@email.com", "qwer1234!"));
        ReservationTime time = timeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(
                Theme.createWithoutId("테마", "설명", "섬네일"));

        String accessToken = tokenProvider.createAccessToken(
                new AccessTokenContent(admin.getId(), admin.getRole(), admin.getName()));

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .cookie("access", accessToken)
                .when().delete("/themes/" + theme.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
