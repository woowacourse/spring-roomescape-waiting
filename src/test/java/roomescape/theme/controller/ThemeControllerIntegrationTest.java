package roomescape.theme.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
public class ThemeControllerIntegrationTest {

    private final DatabaseHelper helper;
    private final JdbcTemplate jdbcTemplate;

    public ThemeControllerIntegrationTest(DatabaseHelper helper, JdbcTemplate jdbcTemplate) {
        this.helper = helper;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void insertReservationDirectly(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, java.sql.Date.valueOf(date), timeId, themeId
        );
    }

    @BeforeEach
    void setup() {
        helper.clear();
    }

    @Test
    @DisplayName("직전 period 일 동안의 예약 수를 기준으로 상위 limit 개의 테마들을 조회한다.")
    void readPopular_returnsPopularThemes() {
        // given
        createReservationTime("10:00");

        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");
        createTheme("페어 테마", "페어 전용 테마입니다.", "https://example.com/pair.png");
        createTheme("당근 테마", "당근 전용 테마입니다.", "https://example.com/carrot.png");

        // 오늘 기준으로 직전 7일 범위 내 날짜에 예약 삽입
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        LocalDate eightDaysAgo = LocalDate.now().minusDays(8); // 범위 밖

        insertReservationDirectly("브라운", yesterday, 1L, 1L);           // 테마1 - 범위 내
        insertReservationDirectly("포비", twoDaysAgo, 1L, 1L);            // 테마1 - 범위 내
        insertReservationDirectly("이든", twoDaysAgo, 1L, 2L);            // 테마2 - 범위 내
        insertReservationDirectly("경계포함예약", sevenDaysAgo, 1L, 2L);  // 테마2 - 경계 포함 (범위 내)
        insertReservationDirectly("오늘예약", LocalDate.now(), 1L, 3L);    // 오늘은 제외 (to = yesterday)
        insertReservationDirectly("범위밖예약", eightDaysAgo, 1L, 3L);    // 범위 밖

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes?popular=true&period=7&limit=2")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("우아한 테마"))
                .body("[1].name", is("페어 테마"))
                .body("name", not(hasItem("당근 테마")));
    }
}
