package roomescape.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.fixture.IntegrationFixture.FUTURE_DATE;
import static roomescape.fixture.IntegrationFixture.PASSWORD;
import static roomescape.fixture.IntegrationFixture.REGULAR2_EMAIL;
import static roomescape.fixture.IntegrationFixture.REGULAR_EMAIL;
import static roomescape.fixture.IntegrationFixture.TOKEN;
import static roomescape.fixture.IntegrationFixture.createRegularReservation;
import static roomescape.fixture.IntegrationFixture.createReservationTime;
import static roomescape.fixture.IntegrationFixture.createTheme;
import static roomescape.fixture.IntegrationFixture.loginAndGetAuthToken;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.common.security.dto.request.LoginRequest;
import roomescape.common.security.dto.response.CheckLoginResponse;
import roomescape.fixture.IntegrationFixture;
import roomescape.reservation.presentation.dto.response.MyReservationResponse;
import roomescape.reservation.presentation.dto.response.WaitingReservationResponse;
import roomescape.waiting.domain.WaitingStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
public class RegularTest {

    private String REGULAR_TOKEN;

    @BeforeEach
    void setUp() {
        REGULAR_TOKEN = loginAndGetAuthToken(REGULAR_EMAIL, PASSWORD);
    }

    @Test
    void logout() {
        RestAssured.given().log().all()
                .body(new LoginRequest(REGULAR_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(TOKEN, REGULAR_TOKEN)
                .when().post("/logout")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void loginCheck() {
        CheckLoginResponse checkLoginResponse = RestAssured.given().log().all()
                .body(new LoginRequest(REGULAR_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(TOKEN, REGULAR_TOKEN)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckLoginResponse.class);

        assertThat(checkLoginResponse.name()).isEqualTo("Regular");
    }


    @Test
    void createAndDeleteReservation() {
        IntegrationFixture.createReservationTime();
        createTheme("추리");
        createRegularReservation(1L);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie(TOKEN, REGULAR_TOKEN)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void responseUnAuthorizedWhenRegularAccessAdminPage() {
        RestAssured.given().log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void responseForbiddenWhenRegularAccessAdminPage() {
        RestAssured.given().log().all()
                .cookie(TOKEN, REGULAR_TOKEN)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void createWaitingReservations() {
        createReservationTime();
        createTheme("추리");
        createRegularReservation(1L);

        String user2Token = loginAndGetAuthToken(REGULAR2_EMAIL, PASSWORD);
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        WaitingReservationResponse waitingReservationResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TOKEN, user2Token)
                .body(reservation)
                .when().post("/waiting-reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(WaitingReservationResponse.class);

        assertThat(waitingReservationResponse.waitingStatus()).isEqualTo(WaitingStatus.WAITING);
    }

    @Test
    void findMyReservations() {
        createWaitingReservations();
        String user2Token = loginAndGetAuthToken(REGULAR2_EMAIL, PASSWORD);

        List<MyReservationResponse> responses = RestAssured.given().log().all()
                .cookie(TOKEN, user2Token)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(responses.size()).isEqualTo(1);
            softAssertions.assertThat(responses.getFirst().status()).isEqualTo("1번째 예약대기");
        });
    }

    @Test
    void removeWaitingReservations() {
        createReservationTime();
        createTheme("추리");
        createRegularReservation(1L);

        String user2Token = loginAndGetAuthToken(REGULAR2_EMAIL, PASSWORD);
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        WaitingReservationResponse waitingReservationResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TOKEN, user2Token)
                .body(reservation)
                .when().post("/waiting-reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(WaitingReservationResponse.class);

        Long reservationId = waitingReservationResponse.reservationId();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TOKEN, user2Token)
                .pathParam("reservationId", reservationId)
                .when().delete("/waiting-reservations/{reservationId}")
                .then().log().all()
                .statusCode(204);

        List<MyReservationResponse> responses = RestAssured.given().log().all()
                .cookie(TOKEN, user2Token)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
        assertThat(responses).isEmpty();
    }
}
