package roomescape.e2e;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class PopularThemeE2ETest extends E2ETest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Clock clock;

    @DisplayName("5월 1일 기준, 직전 period 일 동안의 예약 수를 기준으로 상위 limit 개의 테마들을 조회한다.")
    @Test
    void readPopular() {
        // given
        createTime(LocalTime.of(10, 0));

        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/woowa.png");
        createTheme("페어 테마", "페어 전용 테마입니다.", "https://example.com/pair.png");
        createTheme("당근 테마", "당근 전용 테마입니다.", "https://example.com/carrot.png");

        createReservation("brown", LocalDate.of(2026, 4, 29), 1L, 1L);
        createReservation("pobi", LocalDate.of(2026, 4, 30), 1L, 1L);
        createReservation("eden", LocalDate.of(2026, 4, 30), 1L, 2L);
        createReservation("boundaryReservation", LocalDate.of(2026, 4, 24), 1L, 2L);
        createReservation("todayReservation", LocalDate.of(2026, 5, 1), 1L, 3L);
        createReservation("outOfRangeReservation", LocalDate.of(2026, 4, 23), 1L, 3L);

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

        assertThat(LocalDate.of(2026, 5, 1))
                .isEqualTo(LocalDate.now(clock));
    }

    private void createTime(LocalTime time) {
        jdbcTemplate.update("""
            insert into reservation_time(start_at)
            values (?)
        """, time
        );
    }

    private void createTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update("""
            insert into theme(name, description, thumbnail_url)
            values (?, ?, ?)
        """, name, description, thumbnailUrl
        );
    }

    private void createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update("""
            insert into reservation(name, reservation_date, time_id, theme_id)
            values (?, ?, ?, ?)
        """, name, date,timeId, themeId
        );

    }
}
