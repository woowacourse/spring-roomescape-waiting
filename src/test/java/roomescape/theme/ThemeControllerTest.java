package roomescape.theme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestTimeConfig.class)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ThemeControllerTest {

    @LocalServerPort
    int port;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String login() {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", "testAdmin");
        loginRequest.put("password", "test2");
        loginRequest.put("storeId", 1L);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data.accessToken");
    }

    @Test
    void 테마_저장_API_테스트() {
        String accessToken = login();
        Map<String, String> params = new HashMap<>();
        params.put("name", "무서운게 딱 좋아");
        params.put("description", "무서운 분위기의 방탈출");
        params.put("thumbnailUrl", "https://example.com/theme.jpg");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/manager/themes")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 테마_추가_및_삭제_API_테스트() {
        String accessToken = login();
        Map<String, String> params = new HashMap<>();
        params.put("name", "무서운게 딱 좋아");
        params.put("description", "무서운 분위기의 방탈출");
        params.put("thumbnailUrl", "https://example.com/theme.jpg");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/manager/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/manager/themes/5")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 각_날짜에_존재하는_모든_테마_조회_API_테스트() {
        String accessToken = login();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("date", "2026-05-05")
                .when().get("/api/themes")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4))
                .body("data[0].id", is(1))
                .body("data[1].id", is(2))
                .body("data[2].id", is(3))
                .body("data[3].id", is(4));
    }

    @Test
    void 최근_7일_예약_개수에_따른_인기_테마_조회_API_테스트() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 7)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        RestAssured.given().log().all()
                .when().get("/api/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(3))
                .body("data[0].id", is(2))
                .body("data[1].id", is(1))
                .body("data[2].id", is(3));
    }

    @Test
    void 테마_전체_조회_API_테스트() {
        String accessToken = login();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/themes")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4))
                .body("data[0].id", is(1))
                .body("data[1].id", is(2))
                .body("data[2].id", is(3))
                .body("data[3].id", is(4));
    }
}
