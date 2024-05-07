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
import roomescape.controller.request.ReservationTimeRequest;
import roomescape.model.ReservationTime;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeControllerTest {

    private static final int INITIAL_TIME_COUNT = 2;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert timeInsertActor;

    @Autowired
    public ReservationTimeControllerTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertReservationTime("1:00");
        insertReservationTime("2:00");
    }

    private void initDatabase() {
        jdbcTemplate.execute("ALTER TABLE reservation_time SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void should_get_all_reservation_times() {
        List<ReservationTime> reservationTimes = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ReservationTime.class);

        assertThat(reservationTimes).hasSize(INITIAL_TIME_COUNT);
    }

    @DisplayName("예약 시간을 추가한다.")
    @Test
    void should_add_reservation_time() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(3, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/times/" + (INITIAL_TIME_COUNT + 1));

        assertThat(countAllTimes()).isEqualTo(INITIAL_TIME_COUNT + 1);
    }

    @DisplayName("예약 시간을 삭제한다.")
    @Test
    void should_remove_reservation_time() {
        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);

        assertThat(countAllTimes()).isEqualTo(INITIAL_TIME_COUNT - 1);
    }

    private Integer countAllTimes() {
        return jdbcTemplate.queryForObject("SELECT count(id) from reservation_time", Integer.class);
    }
}
