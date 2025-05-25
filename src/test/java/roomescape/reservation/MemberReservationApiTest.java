package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.login.application.TokenCookieService;
import roomescape.login.application.dto.LoginRequest;
import roomescape.reservation.application.dto.MemberReservationRequest;

@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MemberReservationApiTest {

    @LocalServerPort
    private int port;
    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        final String email = "test1@test.com";
        final String password = "1234";

        final LoginRequest request = new LoginRequest(email, password);

        token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];
    }

    @Test
    void 예약을_추가한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약날짜는_null을_받을_수_없다() {
        final MemberReservationRequest request = new MemberReservationRequest(null, 1L, 1L);

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }


    @Test
    void 예약_시간_id는_null을_받을_수_없다() {
        final MemberReservationRequest request = new MemberReservationRequest(LocalDate.now().plusDays(1), null, 1L);

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 과거날짜로_예약을_하면_에러를_반환한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().minusDays(10),
                1L,
                1L
        );

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(equalTo("현재보다 과거의 날짜로 예약할 수 없습니다."));
    }

    @Test
    void 사용자가_본인의_예약을_조회한다() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .when().get("/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }

    @Test
    void 테마_id가_null이면_에러를_반환한다() {
        final MemberReservationRequest request = new MemberReservationRequest(LocalDate.now().plusDays(1), 1L, null);

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 로그인을_안한상태로_본인의_예약을_조회하면_401를_반환한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/mine")
                .then().log().all()
                .statusCode(401);
    }
}
