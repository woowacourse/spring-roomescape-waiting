package roomescape.theme.controller;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
public class ThemeControllerIntegrationTest {

    @Autowired
    Clock clock;

    @Autowired
    DatabaseHelper helper;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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
    @DisplayName("5월 1일 기준, 직전 period 일 동안의 예약 수를 기준으로 상위 limit 개의 테마들을 조회한다.")
    void readPopular_returnsPopularThemes() {
        // given
        createReservationTime("10:00");

        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");
        createTheme("페어 테마", "페어 전용 테마입니다.", "https://example.com/pair.png");
        createTheme("당근 테마", "당근 전용 테마입니다.", "https://example.com/carrot.png");

        insertReservationDirectly("브라운", LocalDate.of(2026, 4, 29), 1L, 1L);
        insertReservationDirectly("포비", LocalDate.of(2026, 4, 30), 1L, 1L);
        insertReservationDirectly("이든", LocalDate.of(2026, 4, 30), 1L, 2L);
        insertReservationDirectly("경계포함예약", LocalDate.of(2026, 4, 24), 1L, 2L);
        insertReservationDirectly("오늘예약", LocalDate.of(2026, 5, 1), 1L, 3L);
        insertReservationDirectly("범위밖예약", LocalDate.of(2026, 4, 23), 1L, 3L);

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

        assertThat(LocalDate.now(clock)).isEqualTo(LocalDate.of(2026, 5, 1));
    }
}
