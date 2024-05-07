package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.controller.request.ThemeRequest;
import roomescape.controller.response.ThemeResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ThemeControllerTest {

    private static final int INITIAL_THEME_COUNT = 20;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;

    @Autowired
    public ThemeControllerTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.reservationInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        IntStream.range(1, 3).forEach(i -> insertReservationTime(i + ":00"));
        IntStream.range(1, 21).forEach(i -> insertTheme("n" + i, "d" + i, "t" + i));

        LocalDate now = LocalDate.now();
        IntStream.range(1, 2).forEach(i -> insertReservation(now.minusDays(i), 1L, 1L, 1L));
        IntStream.range(1, 3).forEach(i -> insertReservation(now.minusDays(i), 1L, 2L, 1L));
        IntStream.range(1, 4).forEach(i -> insertReservation(now.minusDays(i), 1L, 3L, 1L));
        IntStream.range(1, 5).forEach(i -> insertReservation(now.minusDays(i), 2L, 4L, 1L));
        IntStream.range(1, 6).forEach(i -> insertReservation(now.minusDays(i), 2L, 5L, 1L));
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    private void insertReservation(LocalDate date, long timeId, long themeId, long memberId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        reservationInsertActor.execute(parameters);
    }

    @DisplayName("전체 테마를 조회한다.")
    @Test
    void should_get_all_themes() {
        List<ThemeResponse> themes = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ThemeResponse.class);

        assertThat(themes).hasSize(INITIAL_THEME_COUNT);
    }

    @DisplayName("테마를 추가한다.")
    @Test
    void should_add_theme() {
        ThemeRequest request = new ThemeRequest("n", "d", "t");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/themes/" + (INITIAL_THEME_COUNT + 1));

        assertThat(countAllThemes()).isEqualTo(INITIAL_THEME_COUNT + 1);
    }

    @DisplayName("테마를 삭제한다")
    @Test
    void should_remove_theme() {
        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);

        assertThat(countAllThemes()).isEqualTo(INITIAL_THEME_COUNT - 1);
    }

    @DisplayName("일주일 간 예약된 테마를 인기 순으로 10개 조회한다. 예약 횟수가 같은 경우 theme_id 오름차순으로 조회한다.")
    @Test
    void should_find_popular_themes_when_more_than_10() {
        LocalDate date = LocalDate.now().minusDays(1);
        insertReservation(date, 1, 6, 1);
        insertReservation(date, 1, 7, 1);
        insertReservation(date, 1, 8, 1);
        insertReservation(date, 1, 9, 1);
        insertReservation(date, 1, 10, 2);
        insertReservation(date, 1, 11, 2);
        insertReservation(date, 1, 12, 2);

        List<ThemeResponse> popularThemes = RestAssured.given().log().all()
                .when().get("/themes/rank")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ThemeResponse.class);

        assertAll(() -> {
            assertThat(popularThemes).hasSize(10);
            assertThat(popularThemes.get(0).getId()).isEqualTo(5);
            assertThat(popularThemes.get(1).getId()).isEqualTo(4);
            assertThat(popularThemes.get(2).getId()).isEqualTo(3);
            assertThat(popularThemes.get(3).getId()).isEqualTo(2);
            assertThat(popularThemes.get(4).getId()).isEqualTo(1);
            assertThat(popularThemes.get(5).getId()).isEqualTo(6);
            assertThat(popularThemes.get(6).getId()).isEqualTo(7);
            assertThat(popularThemes.get(7).getId()).isEqualTo(8);
            assertThat(popularThemes.get(8).getId()).isEqualTo(9);
            assertThat(popularThemes.get(9).getId()).isEqualTo(10);
        });
    }

    @DisplayName("예약된 전체 테마 개수가 10 이하인 경우, 해당 개수만큼의 인기 테마를 인기 순으로 조회한다.")
    @Test
    void should_find_popular_themes_when_less_than_10() {
        List<ThemeResponse> popularThemes = RestAssured.given().log().all()
                .when().get("/themes/rank")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ThemeResponse.class);

        assertAll(() -> {
            assertThat(popularThemes).hasSize(5);
            assertThat(popularThemes.get(0).getId()).isEqualTo(5);
            assertThat(popularThemes.get(1).getId()).isEqualTo(4);
            assertThat(popularThemes.get(2).getId()).isEqualTo(3);
            assertThat(popularThemes.get(3).getId()).isEqualTo(2);
            assertThat(popularThemes.get(4).getId()).isEqualTo(1);
        });
    }

    private Integer countAllThemes() {
        return jdbcTemplate.queryForObject("SELECT count(id) from theme", Integer.class);
    }
}
