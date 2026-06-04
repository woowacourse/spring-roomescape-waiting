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
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("예약 목록을 조회한다")
    void getReservations_success() {
        RestAssured.given().log().all()
                .header("Authorization", "ADMIN")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0)); // 아직 생성 요청이 없으니 0개
    }

    @Test
    @DisplayName("데이터베이스 연결을 확인한다")
    void connectDatabase_success() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("예약 시간 관리 기능을 처리한다")
    void manageReservationTime_success() {
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
    @DisplayName("예약과 예약 시간을 연결한다")
    void connectReservationAndTime_success() {
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
    @DisplayName("예약 대기를 저장한다")
    void saveWaiting_success() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1).toString());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "김인직", "레전드 방송", "gamst.jpg");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "리오",
                TODAY.plusDays(1).toString(),
                1,
                1
        );

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
                .body("theme.id", is(1))
                .body("rank", is(1));
    }

    @Test
    @DisplayName("같은 슬롯에 여러 사용자가 예약 대기를 신청할 수 있다")
    void saveWaiting_success_when_other_user_waiting_exists() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1).toString());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "김인직", "레전드 방송", "gamst.jpg");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "리오",
                TODAY.plusDays(1).toString(),
                1,
                1
        );

        Map<String, Object> firstWaiting = new HashMap<>();
        firstWaiting.put("name", "브라운");
        firstWaiting.put("date", TODAY.plusDays(1).toString());
        firstWaiting.put("timeId", 1);
        firstWaiting.put("themeId", 1);

        Map<String, Object> secondWaiting = new HashMap<>();
        secondWaiting.put("name", "흑곰");
        secondWaiting.put("date", TODAY.plusDays(1).toString());
        secondWaiting.put("timeId", 1);
        secondWaiting.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(firstWaiting)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201)
                .body("rank", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(secondWaiting)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201)
                .body("name", is("흑곰"))
                .body("rank", is(2));
    }

    @Test
    @DisplayName("예약이 없는 슬롯에는 대기할 수 없다")
    void saveWaiting_fail_with_not_found_reservation() {
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
                .statusCode(400)
                .body("message", is("예약이 존재하지 않으면, 대기요청을 할 수 없습니다."));
    }

    @Test
    @DisplayName("예약 대기를 삭제한다")
    void deleteWaiting_success() {
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
    @DisplayName("내 예약 목록에서 예약과 대기를 함께 조회한다")
    void getMyReservations_success_when_waiting_exists() {
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

    @Test
    @DisplayName("예약이 취소되면 1순위 대기가 예약으로 자동 전환되고 남은 대기 순번이 재정렬된다")
    void cancelReservation_success_when_waiting_auto_promoted() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1).toString());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "김인직", "레전드 방송", "gamst.jpg");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "리오",
                TODAY.plusDays(1).toString(),
                1,
                1
        );
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, DATEADD('SECOND', -1, CURRENT_TIMESTAMP))",
                "브라운",
                TODAY.plusDays(1).toString(),
                1,
                1
        );
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "흑곰",
                TODAY.plusDays(1).toString(),
                1,
                1
        );

        RestAssured.given().log().all()
                .queryParam("name", "리오")
                .when().delete("/reservations/me/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("브라운"))
                .body("waitings.size()", is(0));

        RestAssured.given().log().all()
                .queryParam("name", "흑곰")
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("waitings.size()", is(1))
                .body("waitings[0].name", is("흑곰"))
                .body("waitings[0].rank", is(1));
    }

    @Test
    @DisplayName("관리자가 예약을 삭제하면 1순위 대기가 예약으로 자동 전환되고 남은 대기 순번이 재정렬된다")
    void deleteReservation_success_when_waiting_auto_promoted() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1).toString());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "김인직", "레전드 방송", "gamst.jpg");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "리오",
                TODAY.plusDays(1).toString(),
                1,
                1
        );
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, DATEADD('SECOND', -1, CURRENT_TIMESTAMP))",
                "브라운",
                TODAY.plusDays(1).toString(),
                1,
                1
        );
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "흑곰",
                TODAY.plusDays(1).toString(),
                1,
                1
        );

        RestAssured.given().log().all()
                .header("Authorization", "ADMIN")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("브라운"))
                .body("waitings.size()", is(0));

        RestAssured.given().log().all()
                .queryParam("name", "흑곰")
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("waitings.size()", is(1))
                .body("waitings[0].name", is("흑곰"))
                .body("waitings[0].rank", is(1));
    }

    @Autowired
    private ReservationController reservationController;

    @Test
    @DisplayName("컨트롤러가 저장소 기술에 직접 의존하지 않는다")
    void layeredArchitecture_success() {
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
