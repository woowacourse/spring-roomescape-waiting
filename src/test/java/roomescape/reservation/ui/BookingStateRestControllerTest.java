package roomescape.reservation.ui;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.ui.LoginApiFixture;
import roomescape.reservation.domain.BookingState;
import roomescape.reservation.ui.dto.response.BookingStateResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
class BookingStateRestControllerTest {

    @Test
    void 예약_상태_목록을_조회한다() {
        final Map<String, String> adminCookies = LoginApiFixture.adminLoginAndGetCookies();

        final List<BookingStateResponse> responses =
                RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .cookies(adminCookies)
                        .when().get("/statuses")
                        .then().log().all()
                        .extract().jsonPath()
                        .getList(".", BookingStateResponse.class);

        assertThat(responses).hasSize(BookingState.values().length);
    }
}
