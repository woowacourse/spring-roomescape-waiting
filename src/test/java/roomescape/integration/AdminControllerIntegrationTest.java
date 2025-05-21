package roomescape.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.presentation.dto.LoginRequest;
import roomescape.presentation.dto.ReservationRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminControllerIntegrationTest {

    @LocalServerPort
    private int port;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("올바른 예약 정보로 요청하면 예약이 성공적으로 생성된다")
    void createReservation_WithValidRequest_ReturnsCreatedReservation() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "ADMIN", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, 1L, 1L, 1L);

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/admin/reservations")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("member.id", equalTo(1))
                .body("theme.id", equalTo(1))
                .body("date", equalTo(LocalDate.now().plusDays(1).toString()))
                .body("time.startAt", equalTo("14:00:00"));
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 예약을 시도하면 404 상태코드를 응답한다")
    void createReservation_WithNonExistentMember_Returns404() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "ADMIN", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, 999L, 1L, 1L);

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/admin/reservations")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID로 예약을 시도하면 404 상태코드를 응답한다")
    void createReservation_WithNonExistentTheme_Returns404() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "ADMIN", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, 1L, 1L, 999L);

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/admin/reservations")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("과거 날짜로 예약을 시도하면 400 상태코드를 응답한다")
    void createReservation_WithPastDate_Returns400() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "ADMIN", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate pastDate = LocalDate.now().minusDays(1);
        final ReservationRequest request = new ReservationRequest(pastDate, 1L, 1L, 1L);

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/admin/reservations")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("이미 예약된 시간에 예약을 시도하면 409 상태코드를 응답한다")
    void createReservation_WithDuplicateDateTime_Returns409() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "ADMIN", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final ReservationRequest request = new ReservationRequest(futureDate, 1L, 1L, 1L);
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/admin/reservations");

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/admin/reservations")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("잘못된 형식의 요청을 보내면 400 상태코드를 응답한다")
    void createReservation_WithInvalidRequest_Returns400() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(reservationTime);

        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        final Member member = new Member("이름", "ADMIN", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final String invalidRequest = """
                {
                    date: 'invalidDate',
                    memberId: 1,
                    timeId: 1,
                    themeId: 1
                }
                """;

        // when & then
        given()
                .cookie("token", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidRequest)
                .when()
                .post("/admin/reservations")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
