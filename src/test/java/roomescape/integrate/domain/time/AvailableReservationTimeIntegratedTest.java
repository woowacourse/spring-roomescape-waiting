package roomescape.integrate.domain.time;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.integrate.fixture.RequestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class AvailableReservationTimeIntegratedTest {

    private final RequestFixture requestFixture = new RequestFixture();
    private String todayDateString;
    private Map<String, String> cookies;

    @BeforeEach
    void setup() {
        todayDateString = LocalDate.now().plusDays(1).toString();

        requestFixture.reqeustSignup("투다", "reservation-add@email.com", "password");
        cookies = requestFixture.requestLogin("reservation-add@email.com", "password");
        requestFixture.requestAddTime(LocalTime.now().plusHours(1).toString());
        requestFixture.requestAddTime(LocalTime.now().plusHours(2).toString());
        long timeId = requestFixture.requestAddTime(LocalTime.now().plusHours(3).toString());
        long themeId = requestFixture.requestAddTheme("테마 명", "description", "thumbnail");
        requestFixture.requestAddReservation("브라운", todayDateString, timeId, themeId, cookies);
    }

    @Test
    void 예약_가능한_시간을_확인할_수_있다() {
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/available-times?date=" + todayDateString + "&themeId=1")
                .then().log().all()
                .statusCode(200)
                .extract().response();

        List<Boolean> alreadyBooked = response.jsonPath().getList("alreadyBooked", Boolean.class);
        List<Boolean> booleans = List.of(false, false, true);
        assertThat(alreadyBooked).containsAnyElementsOf(booleans);
    }
}
