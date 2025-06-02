package roomescape.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.business.domain.WaitInfo;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.persistence.repository.WaitInfoRepository;
import roomescape.presentation.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationsMineControllerIntegrationTest {

    @LocalServerPort
    private int port;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private WaitInfoRepository waitInfoRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("로그인한 사용자의 예약 목록을 조회하면 200 상태코드와 함께 예약 목록이 반환된다")
    void getUserReservations_WithValidRequest_ReturnsReservations() {
        // given
        final ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(time1);
        final ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        reservationTimeRepository.save(time2);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member1 = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member1);
        final Member member2 = new Member("브라운", "USER", "braun@test.com", "pass");
        memberRepository.save(member2);

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final Reservation reservation1 = new Reservation(futureDate, time1, theme);
        reservationRepository.save(reservation1);
        final Reservation reservation2 = new Reservation(futureDate, time2, theme);
        reservationRepository.save(reservation2);

        final WaitInfo member1WaitInfoRank1 = new WaitInfo(member1, reservation1, 1L);
        waitInfoRepository.save(member1WaitInfoRank1);
        final WaitInfo member2WaitInfoRank2 = new WaitInfo(member2, reservation1, 2L);
        waitInfoRepository.save(member2WaitInfoRank2);

        final WaitInfo member2WaitInfoRank1 = new WaitInfo(member2, reservation2, 1L);
        waitInfoRepository.save(member2WaitInfoRank1);
        final WaitInfo member1WaitInfoRank2 = new WaitInfo(member1, reservation2, 2L);
        waitInfoRepository.save(member1WaitInfoRank2);

        final LoginRequest loginRequest = new LoginRequest("fuyu@test.com", "pass");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        // when & then
        given()
                .cookie("token", token)
                .when()
                .get("/reservations-mine")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(2));
    }

    @Test
    @DisplayName("사용자가 예약을 삭제할 때 성공적으로 삭제된다")
    void deleteReservation_WithValidRequest_ReturnsNoContent() {
        // given
        final ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(time1);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member1 = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member1);

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final Reservation reservation1 = new Reservation(futureDate, time1, theme);
        reservationRepository.save(reservation1);

        final WaitInfo waitInfo = new WaitInfo(member1, reservation1, 1L);
        waitInfoRepository.save(waitInfo);

        final LoginRequest loginRequest = new LoginRequest("fuyu@test.com", "pass");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        // when & then
        given()
                .cookie("token", token)
                .when()
                .delete("/reservations-mine/" + waitInfo.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("사용자가 존재하지 않는 예약을 삭제하면 404 상태코드를 응답한다")
    void deleteReservation_WithNonExistentWaitInfo_ReturnsNotFound() {
        // given
        final Member member1 = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member1);

        final Long notExistWaitInfoId = 999L;

        final LoginRequest loginRequest = new LoginRequest("fuyu@test.com", "pass");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        // when & then
        given()
                .cookie("token", token)
                .when()
                .delete("/reservations-mine/" + notExistWaitInfoId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("사용자가 본인의 것이 아닌 예약을 삭제하면 404 상태코드를 응답한다")
    void deleteReservation_WithDifferentMember_ReturnsNotFound() {
        // given
        final ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(time1);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member1 = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member1);
        final Member member2 = new Member("브라운", "USER", "braun@test.com", "pass");
        memberRepository.save(member2);

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final Reservation reservation1 = new Reservation(futureDate, time1, theme);
        reservationRepository.save(reservation1);

        final WaitInfo waitInfo = new WaitInfo(member1, reservation1, 1L);
        waitInfoRepository.save(waitInfo);

        final LoginRequest loginRequest = new LoginRequest("braun@test.com", "pass");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        // when & then
        given()
                .cookie("token", token)
                .when()
                .delete("/reservations-mine/" + waitInfo.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
