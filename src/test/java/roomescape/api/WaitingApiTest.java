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
import roomescape.domain.Waiting;
import roomescape.dto.business.AccessTokenContent;
import roomescape.dto.business.WaitingCreationContent;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.utility.JwtTokenProvider;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Rollback(value = false)
class WaitingApiTest {

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
    private WaitingRepository waitingRepository;
    @Autowired
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        waitingRepository.deleteAll();
        memberRepository.deleteAll();
        timeRepository.deleteAll();
        themeRepository.deleteAll();
    }

    @DisplayName("모든 예약 대기를 조회할 수 있다.")
    @Test
    void canFindAllWaiting() {
        // given
        Member admin = memberRepository.save(
                Member.createWithoutId(Role.ADMIN, "관리자", "admin@email.com", "qwer1234!"));
        Member member = memberRepository.save(
                Member.createWithoutId(Role.GENERAL, "회원", "member@email.com", "qwer1234!"));
        ReservationTime time = timeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(
                Theme.createWithoutId("테마", "설명", "섬네일"));

        reservationRepository.save(Reservation.createWithoutId(YESTERDAY, time, theme, member));
        reservationRepository.save(Reservation.createWithoutId(TODAY, time, theme, member));
        reservationRepository.save(Reservation.createWithoutId(NEXT_DAY, time, theme, member));

        waitingRepository.save(Waiting.createWithoutId(YESTERDAY, theme, time, member));
        waitingRepository.save(Waiting.createWithoutId(TODAY, theme, time, member));
        waitingRepository.save(Waiting.createWithoutId(NEXT_DAY, theme, time, member));

        AccessTokenContent tokenContent = new AccessTokenContent(admin.getId(), admin.getRole(), admin.getName());
        String accessToken = tokenProvider.createAccessToken(tokenContent);

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .cookie("access", accessToken)
                .when().get("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(3));
    }

    @DisplayName("예약 대기를 추가할 수 있다.")
    @Test
    void canAddWaiting() {
        // given
        Member member = memberRepository.save(
                Member.createWithoutId(Role.GENERAL, "회원", "member@email.com", "qwer1234!"));
        ReservationTime time = timeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(
                Theme.createWithoutId("테마", "설명", "섬네일"));
        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId(NEXT_DAY, time, theme, member));

        AccessTokenContent tokenContent = new AccessTokenContent(member.getId(), member.getRole(), member.getName());
        String accessToken = tokenProvider.createAccessToken(tokenContent);

        WaitingCreationContent creationContent =
                new WaitingCreationContent(NEXT_DAY, theme.getId(), time.getId(), member.getId());

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .cookie("access", accessToken)
                .body(creationContent)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }


    @DisplayName("ID를 통해 예약 대기를 삭제가할 수 있다.")
    @Test
    void canDeleteWaiting() {
        // given
        Member member = memberRepository.save(
                Member.createWithoutId(Role.GENERAL, "회원", "member@email.com", "qwer1234!"));
        ReservationTime time = timeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(
                Theme.createWithoutId("테마", "설명", "섬네일"));
        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId(NEXT_DAY, time, theme, member));
        Waiting waiting = waitingRepository.save(Waiting.createWithoutId(NEXT_DAY, theme, time, member));

        AccessTokenContent tokenContent = new AccessTokenContent(member.getId(), member.getRole(), member.getName());
        String accessToken = tokenProvider.createAccessToken(tokenContent);

        // when & then
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .cookie("access", accessToken)
                .when().delete("/waiting/" + waiting.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
