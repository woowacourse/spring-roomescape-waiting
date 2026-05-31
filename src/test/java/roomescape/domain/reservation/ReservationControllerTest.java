package roomescape.domain.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long futureDateId;
    private Long pastDateId;
    private Long todayDateId;
    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        KeyHolder futureDateKeyHolder = new GeneratedKeyHolder();
        String futureDate = LocalDate.now().plusDays(10).toString();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_date(play_day) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, futureDate);
            return ps;
        }, futureDateKeyHolder);
        futureDateId = Objects.requireNonNull(futureDateKeyHolder.getKey()).longValue();

        KeyHolder pastDateKeyHolder = new GeneratedKeyHolder();
        String pastDate = LocalDate.now().minusDays(1).toString();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_date(play_day) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pastDate);
            return ps;
        }, pastDateKeyHolder);
        pastDateId = Objects.requireNonNull(pastDateKeyHolder.getKey()).longValue();

        KeyHolder todayDateKeyHolder = new GeneratedKeyHolder();
        String todayDate = LocalDate.now().toString();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_date(play_day) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, todayDate);
            return ps;
        }, todayDateKeyHolder);
        todayDateId = Objects.requireNonNull(todayDateKeyHolder.getKey()).longValue();

        KeyHolder timeKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_time(start_at) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "22:00");
            return ps;
        }, timeKeyHolder);
        timeId = Objects.requireNonNull(timeKeyHolder.getKey()).longValue();

        KeyHolder themeKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into theme(name, content, url) values (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "테스트테마");
            ps.setString(2, "설명");
            ps.setString(3, "url");
            return ps;
        }, themeKeyHolder);
        themeId = Objects.requireNonNull(themeKeyHolder.getKey()).longValue();
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테스터");
        params.put("dateId", futureDateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("name", is("테스터"));
    }

    @Test
    @DisplayName("과거 시간으로 예약을 생성할 수 없다.")
    void createReservation_Fail_PastTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테스터");
        params.put("dateId", pastDateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("중복된 예약은 생성할 수 없다.")
    void createReservation_Fail_Duplicated() {
        jdbcTemplate.update("insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
            "기존테스터", futureDateId, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "새로운테스터");
        params.put("dateId", futureDateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(409);
    }

    @Test
    @DisplayName("이름으로 예약을 조회한다.")
    void getReservationsByName() {
        jdbcTemplate.update("insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
            "테스터", futureDateId, timeId, themeId);

        RestAssured.given().log().all()
            .param("name", "테스터")
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("any { it.name == '테스터' }", is(true));
    }

    @Test
    @DisplayName("예약을 취소(삭제)한다.")
    void cancelReservation() {
        KeyHolder reservationKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "취소테스터");
            ps.setLong(2, futureDateId);
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, reservationKeyHolder);
        Long reservationId = Objects.requireNonNull(reservationKeyHolder.getKey()).longValue();

        RestAssured.given().log().all()
            .when().delete("/reservations/" + reservationId)
            .then().log().all()
            .statusCode(204);
    }

    @Test
    @DisplayName("당일 예약은 취소(삭제)할 수 없다.")
    void cancelReservation_Fail_Today() {
        KeyHolder reservationKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "취소불가테스터");
            ps.setLong(2, todayDateId);
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, reservationKeyHolder);
        Long reservationId = Objects.requireNonNull(reservationKeyHolder.getKey()).longValue();

        RestAssured.given().log().all()
            .when().delete("/reservations/" + reservationId)
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("예약을 수정한다.")
    void updateReservation() {
        KeyHolder reservationKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "수정테스터");
            ps.setLong(2, futureDateId);
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, reservationKeyHolder);
        Long reservationId = Objects.requireNonNull(reservationKeyHolder.getKey()).longValue();

        KeyHolder newDateKeyHolder = new GeneratedKeyHolder();
        String newFutureDate = LocalDate.now().plusDays(15).toString();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_date(play_day) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newFutureDate);
            return ps;
        }, newDateKeyHolder);
        Long newDateId = Objects.requireNonNull(newDateKeyHolder.getKey()).longValue();

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", newDateId);
        params.put("timeId", timeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + reservationId)
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("당일 예약은 수정할 수 없다.")
    void updateReservation_Fail_Today() {
        KeyHolder reservationKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "당일예약테스터");
            ps.setLong(2, todayDateId);
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, reservationKeyHolder);
        Long reservationId = Objects.requireNonNull(reservationKeyHolder.getKey()).longValue();

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", futureDateId);
        params.put("timeId", timeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + reservationId)
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("과거 시간으로 예약을 수정할 수 없다.")
    void updateReservation_Fail_PastTime() {
        KeyHolder reservationKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "수정테스터");
            ps.setLong(2, futureDateId);
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, reservationKeyHolder);
        Long reservationId = Objects.requireNonNull(reservationKeyHolder.getKey()).longValue();

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", pastDateId);
        params.put("timeId", timeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + reservationId)
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("수정하려는 시간에 이미 다른 예약이 있으면 수정할 수 없다.")
    void updateReservation_Fail_Duplicated() {
        KeyHolder myReservationKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "내예약");
            ps.setLong(2, futureDateId);
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, myReservationKeyHolder);
        Long myReservationId = Objects.requireNonNull(myReservationKeyHolder.getKey()).longValue();

        KeyHolder otherDateKeyHolder = new GeneratedKeyHolder();
        String otherFutureDate = LocalDate.now().plusDays(15).toString();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_date(play_day) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, otherFutureDate);
            return ps;
        }, otherDateKeyHolder);
        Long otherDateId = Objects.requireNonNull(otherDateKeyHolder.getKey()).longValue();

        jdbcTemplate.update("insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
            "다른사람예약", otherDateId, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("dateId", otherDateId);
        params.put("timeId", timeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservations/" + myReservationId)
            .then().log().all()
            .statusCode(409);
    }
}
