package roomescape.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.fixture.IntegrationFixture.FUTURE_DATE;
import static roomescape.fixture.IntegrationFixture.PASSWORD;
import static roomescape.fixture.IntegrationFixture.createReservationTime;
import static roomescape.fixture.IntegrationFixture.createTheme;
import static roomescape.fixture.IntegrationFixture.createRegularReservation;
import static roomescape.fixture.IntegrationFixture.findThemesBySize;
import static roomescape.fixture.IntegrationFixture.loginAndGetAuthToken;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.fixture.TestFixture;
import roomescape.member.presentation.dto.request.SignupRequest;
import roomescape.member.presentation.dto.response.MemberResponse;
import roomescape.reservation.presentation.dto.response.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
public class GuestTest {

    @Test
    void findAllReservations() {
        createReservationTime();
        createTheme("추리");
        createRegularReservation(1L);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

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
                .body(new SignupRequest("testMember@gmail.com", PASSWORD, "testMember"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);

        loginAndGetAuthToken("testMember@gmail.com", PASSWORD);
    }

    @Test
    void findAllRegulars() {
        List<MemberResponse> memberResponses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
        assertThat(memberResponses.size()).isEqualTo(1);
    }

    @Test
    void filterReservations() {
        createReservationTime();
        createTheme("추리");
        createTheme("로맨스");
        createRegularReservation(1L);
        createRegularReservation(2L);

        List<ReservationResponse> reservationsFilteredByThemeId = RestAssured.given().log().all()
                .when().queryParams("themeId", 1L, "memberId", 2L, "dateFrom", FUTURE_DATE,
                        "dateTo", TestFixture.makeFutureDate().plusDays(1).toString())
                .get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
        assertThat(reservationsFilteredByThemeId.size()).isEqualTo(1);
    }
}
