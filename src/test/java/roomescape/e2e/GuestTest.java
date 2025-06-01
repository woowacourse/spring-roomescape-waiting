package roomescape.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.fixture.IntegrationFixture.PASSWORD;
import static roomescape.fixture.IntegrationFixture.REGULAR_EMAIL;
import static roomescape.fixture.IntegrationFixture.TOKEN;
import static roomescape.fixture.IntegrationFixture.createReservationTime;
import static roomescape.fixture.IntegrationFixture.createTheme;
import static roomescape.fixture.IntegrationFixture.findThemesBySize;
import static roomescape.fixture.IntegrationFixture.loginAndGetAuthToken;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.common.security.dto.request.LoginRequest;
import roomescape.common.security.dto.response.CheckLoginResponse;
import roomescape.member.presentation.dto.request.SignupWebRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
public class GuestTest {

    @Test
    void findAvailableReservations() {
        createReservationTime();
        createTheme("추리");

        LocalDate now = LocalDate.now();
        RestAssured.given().log().all()
                .when().queryParams("date", now.toString(), "themeId", 1L).get("/times/available")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void findPopularTheme() {
        createReservationTime();
        createTheme("추리1");
        createTheme("추리2");
        createTheme("추리3");
        createTheme("추리4");
        createTheme("추리5");
        createTheme("추리6");
        createTheme("추리7");
        createTheme("추리8");
        createTheme("추리9");
        createTheme("추리10");
        createTheme("추리11");
        createTheme("추리12");
        findThemesBySize(12);

        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }

    @Test
    void signup() {
        RestAssured.given().log().all()
                .body(new SignupWebRequest("testMember@gmail.com", PASSWORD, "testMember"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);

        loginAndGetAuthToken("testMember@gmail.com", PASSWORD);
    }

    @Test
    void loginCheck() {
        String regularToken = loginAndGetAuthToken(REGULAR_EMAIL, PASSWORD);
        CheckLoginResponse checkLoginResponse = RestAssured.given().log().all()
                .body(new LoginRequest(REGULAR_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(TOKEN, regularToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckLoginResponse.class);

        assertThat(checkLoginResponse.name()).isEqualTo("Regular");
    }
}
