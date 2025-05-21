package roomescape.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.presentation.dto.LoginRequest;
import roomescape.presentation.dto.ReservationRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerIntegrationTest {

    @LocalServerPort
    private int port;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("로그인한 회원이 올바른 예약 정보로 요청하면 예약이 성공적으로 생성된다")
    void createByLoginMember_WithValidRequest_ReturnsCreatedReservation() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "USER", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, null, 1L, 1L);

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/reservations")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("theme.name", equalTo("테마1"))
                .body("date", equalTo(LocalDate.now().plusDays(1).toString()))
                .body("time.startAt", equalTo("14:00:00"));
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하면 200 상태코드와 함께 예약 목록이 반환된다")
    void readAll_ReturnsAllReservations() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/reservations")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", instanceOf(List.class));
    }

    @Test
    @DisplayName("필터 조건으로 예약을 조회하면 조건에 맞는 예약 목록이 반환된다")
    void readFilter_WithValidConditions_ReturnsFilteredReservations() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "USER", "이메일", "비밀번호");
        memberRepository.save(member);

        final Reservation reservation1 = new Reservation(LocalDate.now().plusDays(1), member, reservationTime, theme);
        reservationRepository.save(reservation1);
        final Reservation reservation2 = new Reservation(LocalDate.now().plusDays(2), member, reservationTime, theme);
        reservationRepository.save(reservation2);
        final Reservation reservation3 = new Reservation(LocalDate.now().plusDays(3), member, reservationTime, theme);
        reservationRepository.save(reservation3);

        final Long memberId = 1L;
        final Long themeId = 1L;
        final String formattedDateFrom = LocalDate.now().toString();
        final String formattedDateTo = LocalDate.now().plusDays(2).toString();

        // when & then
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("memberId", memberId)
                .queryParam("themeId", themeId)
                .queryParam("dateFrom", formattedDateFrom)
                .queryParam("dateTo", formattedDateTo)
                .when()
                .get("/reservations/filter")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2));
    }

    @Test
    @DisplayName("존재하는 예약을 삭제하면 204 상태코드를 반환한다")
    void delete_ExistingReservation_ReturnsNoContent() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "USER", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, null, 1L, 1L);
        final Long reservationId = given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/reservations")
                .then()
                .extract()
                .jsonPath()
                .getLong("id");

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete("/reservations/{id}", reservationId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 가능한 예약 시간을 조회하면 200 상태코드와 함께 시간 목록이 반환된다")
    void readAvailableTimes_ReturnsAvailableTimes() {
        // given
        final ReservationTime reservationTime14_00 = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime14_00);

        final ReservationTime reservationTime16_00 = new ReservationTime(LocalTime.of(16, 0));
        reservationTimeRepository.save(reservationTime16_00);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "USER", "이메일", "비밀번호");
        memberRepository.save(member);

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final Reservation reservation14_00 = new Reservation(futureDate, member, reservationTime14_00, theme);
        reservationRepository.save(reservation14_00);

        final String formattedFutureDate = LocalDate.now().plusDays(1).toString();
        final Long themeId = 1L;

        // when & then
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("date", formattedFutureDate)
                .queryParam("themeId", themeId)
                .when()
                .get("/reservations/available-times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("alreadyBooked", hasItems(true, false));
    }

    @Test
    @DisplayName("로그인한 회원의 예약 목록을 조회하면 200 상태코드와 함께 예약 목록이 반환된다")
    void readMine_ReturnsMyReservations() {
        // given
        final ReservationTime reservationTime14_00 = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime14_00);

        final ReservationTime reservationTime16_00 = new ReservationTime(LocalTime.of(16, 0));
        reservationTimeRepository.save(reservationTime16_00);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member1 = new Member("이름1", "USER", "이메일1", "비밀번호1");
        memberRepository.save(member1);
        final Member member2 = new Member("이름2", "USER", "이메일2", "비밀번호2");
        memberRepository.save(member2);

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final Reservation reservation14_00 = new Reservation(futureDate, member1, reservationTime14_00, theme);
        reservationRepository.save(reservation14_00);
        final Reservation reservation16_00 = new Reservation(futureDate, member2, reservationTime16_00, theme);
        reservationRepository.save(reservation16_00);

        final LoginRequest loginRequest = new LoginRequest("이메일1", "비밀번호1");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        // when & then
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .cookie("token", token)
                .when()
                .get("/reservations-mine")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(1))
                .body("[0].date", equalTo(LocalDate.now().plusDays(1).toString()))
                .body("[0].time", equalTo("14:00:00"));
    }

    @Test
    @DisplayName("로그인하지 않은 상태로 예약 생성을 시도하면 401 상태코드를 반환한다")
    void createByLoginMember_WithoutAuth_ReturnsUnauthorized() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름1", "USER", "이메일1", "비밀번호1");
        memberRepository.save(member);

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, null, 1L, 1L);

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/reservations")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하려고 하면 404 상태코드를 반환한다")
    void delete_NonExistingReservation_ReturnsNotFound() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름1", "USER", "이메일1", "비밀번호1");
        memberRepository.save(member);

        // when & then
        given()
                .when()
                .delete("/reservations/{id}", 999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("과거 날짜로 예약을 시도하면 400 상태코드를 반환한다")
    void createByLoginMember_WithPastDate_ReturnsBadRequest() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름1", "USER", "이메일1", "비밀번호1");
        memberRepository.save(member);

        final LocalDate pastDate = LocalDate.now().minusDays(1);
        final ReservationRequest request = new ReservationRequest(pastDate, null, 1L, 1L);

        final LoginRequest loginRequest = new LoginRequest("이메일1", "비밀번호1");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie("token", token)
                .body(request)
                .when()
                .post("/reservations")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("이미 예약된 시간에 예약을 시도하면 409 상태코드를 반환한다")
    void createByLoginMember_WithDuplicateDateTime_ReturnsConflict() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "USER", "이메일", "비밀번호");
        memberRepository.save(member);

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, null, 1L, 1L);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/reservations");

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/reservations")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 시도하면 404 상태코드를 반환한다")
    void createByLoginMember_WithNonExistentTheme_ReturnsNotFound() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "USER", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, null, 1L, 999L);

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/reservations")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
