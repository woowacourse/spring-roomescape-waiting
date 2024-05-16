package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.dto.request.ThemeRequest;

class ThemeTest extends AcceptanceTest {

    @MockBean
    private Clock clock;

    @BeforeEach
    void setClock() {
        given(clock.instant()).willReturn(Instant.parse("2024-05-02T19:19:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
    }

    @DisplayName("ADMIN 테마 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> reservationByAdmin() {
        return Stream.of(
                dynamicTest("테마를 추가한다.", () -> {
                    ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(themeRequest)
                            .when().post("/themes")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("테마를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .when().get("/themes")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                }),

                dynamicTest("테마를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .when().delete("/themes/1")
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }

    @DisplayName("주간 상위 10개 예약 테마 조회 API 테스트")
    @Sql("/init_data/reservationData.sql")
    @Test
    void weeklyTop10Theme() {
        RestAssured.given().log().all()
                .when().get("/themes/ranking")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }
}
