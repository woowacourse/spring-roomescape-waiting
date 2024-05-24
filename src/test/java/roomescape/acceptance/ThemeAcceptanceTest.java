package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.BasicAcceptanceTest;
import roomescape.TestFixtures;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;

class ThemeAcceptanceTest extends BasicAcceptanceTest {
    private String adminToken;

    @BeforeEach
    void SetUp() {
        adminToken = LoginTokenProvider.login("admin@wooteco.com", "wootecoCrew6!", 200);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TestFactory
    @DisplayName("2개의 테마를 추가한다")
    Stream<DynamicTest> themePostTest() {
        return Stream.of(
                dynamicTest("테마를 추가한다", () -> postTheme(adminToken, 201)),
                dynamicTest("테마를 추가한다", () -> postTheme(adminToken, 201)),
                dynamicTest("모든 테마를 조회한다 (총 8개)", () -> getThemes(200, 8))
        );
    }

    @TestFactory
    @DisplayName("테마를 추가하고 삭제한다")
    Stream<DynamicTest> themePostAndDeleteTest() {
        AtomicLong themeId = new AtomicLong();

        return Stream.of(
                dynamicTest("테마를 추가한다 (10:00)", () -> themeId.set(postTheme(adminToken, 201))),
                dynamicTest("테마를 삭제한다 (10:00)", () -> deleteTheme(adminToken, themeId.longValue(), 204)),
                dynamicTest("테마를 추가한다 (10:00)", () -> postTheme(adminToken, 201)),
                dynamicTest("모든 테마를 조회한다 (총 7개)", () -> getThemes(200, 7))
        );
    }

    @TestFactory
    @Sql("/setting-big-reservation.sql")
    @DisplayName("예약 횟수 상위 10개의 테마를 조회한다")
    Stream<DynamicTest> top10Theme() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                dynamicTest("인기 테마를 조회한다", () -> getTopTheme(200, TestFixtures.THEME_RESPONSES_1)),
                dynamicTest("과거 예약을 추가한다", () -> postPastReservation(today.minusDays(5).toString(), "1", "1", "10")),
                dynamicTest("인기 테마를 조회한다", () -> getTopTheme(200, TestFixtures.THEME_RESPONSES_2)),
                dynamicTest("과거 예약을 추가한다", () -> postPastReservation(today.minusDays(4).toString(), "1", "1", "11")),
                dynamicTest("과거 예약을 추가한다", () -> postPastReservation(today.minusDays(4).toString(), "1", "2", "11")),
                dynamicTest("인기 테마를 조회한다", () -> getTopTheme(200, TestFixtures.THEME_RESPONSES_3))
        );
    }

    private Long postTheme(String token, int expectedHttpCode) {
        ThemeRequest themeRequest = new ThemeRequest("테마", "설명서", "썸네일");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", token)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        if (expectedHttpCode == 201) {
            return response.jsonPath().getLong("id");
        }

        return null;
    }

    private void getThemes(int expectedHttpCode, int expectedthemesSize) {
        Response response = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<?> themeResponses = response.as(List.class);

        assertThat(themeResponses).hasSize(expectedthemesSize);
    }

    private void deleteTheme(String token, Long themeId, int expectedHttpCode) {
        RestAssured.given().log().all()
                .cookies("token", token)
                .when().delete("/admin/themes/" + themeId)
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void getTopTheme(int expectedHttpCode, List<ThemeResponse> expectedThemeResponses) {
        Response response = RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<ThemeResponse> themeResponses = response.jsonPath().getList(".", ThemeResponse.class);

        assertThat(themeResponses).isEqualTo(expectedThemeResponses);
    }

    private void postPastReservation(String date, String member_id, String timeId, String themeId) {
        jdbcTemplate.update(
                "insert into reservation (date, member_id, time_id, theme_id) values (?, ?, ?, ?)",
                date, member_id, timeId, themeId);
    }
}
