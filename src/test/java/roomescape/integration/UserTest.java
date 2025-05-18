package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.fixture.IntegrationFixture.FUTURE_DATE;
import static roomescape.fixture.IntegrationFixture.PASSWORD;
import static roomescape.fixture.IntegrationFixture.TOKEN;
import static roomescape.fixture.IntegrationFixture.USER_EMAIL;
import static roomescape.fixture.IntegrationFixture.createReservationTime;
import static roomescape.fixture.IntegrationFixture.createTheme;
import static roomescape.fixture.IntegrationFixture.createUserReservation;
import static roomescape.fixture.IntegrationFixture.loginAndGetAuthToken;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.fixture.IntegrationFixture;
import roomescape.fixture.TestFixture;
import roomescape.global.auth.dto.CheckLoginResponse;
import roomescape.global.auth.dto.LoginRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
public class UserTest {

    private String USER_TOKEN;

    @BeforeEach
    void setUp() {
        USER_TOKEN = loginAndGetAuthToken(USER_EMAIL, PASSWORD);
    }

    @Test
    void login() {
        RestAssured.given().log().all()
                .body(new LoginRequest(USER_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }


    @Test
    void logout() {
        RestAssured.given().log().all()
                .body(new LoginRequest(USER_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(TOKEN, USER_TOKEN)
                .when().post("/logout")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void loginCheck() {
        CheckLoginResponse checkLoginResponse = RestAssured.given().log().all()
                .body(new LoginRequest(USER_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(TOKEN, USER_TOKEN)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckLoginResponse.class);

        assertThat(checkLoginResponse.name()).isEqualTo("User");
    }


    @Test
    void createAndDeleteReservation() {
        IntegrationFixture.createReservationTime();
        createTheme("추리");
        createUserReservation(1L);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie(TOKEN, USER_TOKEN)
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
    void responseUnAuthorizedWhenUserAccessAdminPage() {
        RestAssured.given().log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void responseForbiddenWhenUserAccessAdminPage() {
        RestAssured.given().log().all()
                .cookie(TOKEN, USER_TOKEN)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void findMyReservations() {
        createReservationTime();
        createTheme("추리");
        createUserReservation(1L);

        List<MyReservationResponse> responses = RestAssured.given().log().all()
                .cookie(TOKEN, USER_TOKEN)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertThat(responses.size()).isEqualTo(1);
    }
}
