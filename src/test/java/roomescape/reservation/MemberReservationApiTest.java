package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;

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
import roomescape.login.business.service.TokenCookieService;
import roomescape.login.presentation.request.LoginRequest;
import roomescape.reservation.presentation.request.MemberReservationRequest;

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
                .body(equalTo("현재보다 과거의 날짜로 예약 할 수 없습니다."));
    }


    @Test
    void 중복된_시간에_예약을_하면_에러가_발생한다() {
        final MemberReservationRequest request1 = new MemberReservationRequest(
                LocalDate.now().plusDays(10),
                1L,
                1L
        );

        final MemberReservationRequest request2 = new MemberReservationRequest(
                LocalDate.now().plusDays(10),
                1L,
                1L
        );

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .body(request1)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .contentType(ContentType.JSON)
                .body(request2)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }
}
