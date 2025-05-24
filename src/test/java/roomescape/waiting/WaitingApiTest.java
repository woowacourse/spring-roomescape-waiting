package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.login.application.TokenCookieService;
import roomescape.login.application.dto.LoginRequest;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservation;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
public class WaitingApiTest {

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
    @DisplayName("예약대기를 추가한다")
    void add_waiting() {
        final MemberReservationRequest request = new MemberReservationRequest(
            LocalDate.now().plusDays(1),
            1L,
            1L
        );

        Long waitingId = RestAssured.given().log().all()
            .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/waitings")
            .then().log().all()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("waitingId");

        assertThat(waitingId).isNotNull();
    }

    @Test
    @DisplayName("웨이팅을 추가하고, 삭제한다")
    void add_waiting_and_delete() {
        final MemberReservationRequest request = new MemberReservationRequest(
            LocalDate.now().plusDays(1),
            1L,
            1L
        );

        Long waitingId = RestAssured.given().log().all()
            .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/waitings")
            .then().log().all()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("waitingId");

        RestAssured.given().log().all()
            .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
            .contentType(ContentType.JSON)
            .body(request)
            .when().delete("/waitings/"+waitingId)
            .then().log().all()
            .statusCode(204);

        List<MyReservation> waitings = RestAssured.given().log().all()
            .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
            .contentType(ContentType.JSON)
            .when().get("/waitings")
            .then().log().all()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", MyReservation.class);

        boolean waitingExists = waitings.stream()
            .anyMatch(waiting -> waiting.id().equals(waitingId));
        assertThat(waitingExists).isFalse();
    }

    @Test
    @DisplayName("admin이 아니면, 모든 waiting을 조회할 수 없다.")
    void not_admin_then_cannot_get_waitings() {
        RestAssured.given().log().all()
            .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
            .contentType(ContentType.JSON)
            .when().get("/admin/waitings")
            .then().log().all()
            .statusCode(403);
    }
}
