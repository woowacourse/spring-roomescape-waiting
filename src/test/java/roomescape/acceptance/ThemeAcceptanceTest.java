package roomescape.acceptance;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.response.ThemeResponseDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeAcceptanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("테마 조회 시 저장된 테마 내역을 모두 가져온다")
    void test1() {
        // given
        List<ThemeResponseDto> reservationTimes = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ThemeResponseDto.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from theme", Integer.class);

        // then
        assertThat(reservationTimes.size()).isEqualTo(count);
    }

    @Test
    @DisplayName("정상적으로 테마가 등록되는 경우 201을 반환한다")
    void test2() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "공포");
        params.put("description", "무서워요");
        params.put("thumbnail", "image-url");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 존재하는 테마명을 이용해 등록하고자 한다면 409 를 반환한다.")
    void test3() {
        // given
        String name = "공포";
        insertNewThemeWithJdbcTemplate(name);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", "무서워요");
        params.put("thumbnail", "image-url");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("특정 테마를 삭제하는 경우 성공 시 204를 반환한다")
    void test7() {
        // given
        Long savedId = insertNewThemeWithJdbcTemplate("공포");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/" + savedId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("특정 테마에 대한 예약 내역이 존재하는 경우 삭제를 시도한다면 422 를 반환한다.")
    void test8() {
        // given
        Long savedId = insertNewThemeWithJdbcTemplate("공포");
        insertNewReservationWithJdbcTemplate(1L, savedId);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/" + savedId)
                .then().log().all()
                .statusCode(422);
    }

    private Long insertNewThemeWithJdbcTemplate(final String name) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", new String[]{"id"});
            ps.setString(1, name);
            ps.setString(2, "무서워요");
            ps.setString(3, "image-url");
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private Long insertNewReservationWithJdbcTemplate(final Long timeId, final Long themeId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO reservation (date, time_id, theme_id) VALUES (?, ?, ?)", new String[]{"id"});
            ps.setDate(1, Date.valueOf(tomorrow));
            ps.setLong(2, timeId);
            ps.setLong(3, themeId);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
