package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.presentation.ReservationController;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.execute("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void 예약_조회() {
        RestAssured.given().log().all()
                .header("Authorization", "ADMIN")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0)); // 아직 생성 요청이 없으니 0개
    }

    @Test
    void 데이터베이스_연동() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 시간_관리_API() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약과_시간_연결() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1).toString());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "김인직", "레전드 방송", "gamst.jpg");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", TODAY.plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .header("Authorization", "ADMIN")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 예약_대기_저장() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1).toString());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "김인직", "레전드 방송", "gamst.jpg");

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "브라운");
        waiting.put("date", TODAY.plusDays(1).toString());
        waiting.put("timeId", 1);
        waiting.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("브라운"))
                .body("date", is(TODAY.plusDays(1).toString()))
                .body("time.id", is(1))
                .body("theme.id", is(1));
    }

    @Test
    void 예약_대기_삭제() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1).toString());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "김인직", "레전드 방송", "gamst.jpg");
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "브라운",
                TODAY.plusDays(1).toString(),
                1,
                1
        );

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/waiting/me/1")
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM waiting", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    void 내_예약_목록에서_예약과_대기를_함께_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "테마1", "설명1", "theme1.jpg");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "테마2", "설명2", "theme2.jpg");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "브라운",
                TODAY.plusDays(1).toString(),
                1,
                1
        );
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, DATEADD('SECOND', -1, CURRENT_TIMESTAMP))",
                "리오",
                TODAY.plusDays(2).toString(),
                2,
                2
        );
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "브라운",
                TODAY.plusDays(2).toString(),
                2,
                2
        );

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("브라운"))
                .body("waitings.size()", is(1))
                .body("waitings[0].name", is("브라운"))
                .body("waitings[0].rank", is(2));
    }

    @Autowired
    private ReservationController reservationController;

    @Test
    void 계층화_리팩터링() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
