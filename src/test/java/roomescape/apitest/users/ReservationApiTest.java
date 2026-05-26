package roomescape.apitest.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.config.FixedClockConfig.FUTURE_DATE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationApiTest {
    private final String userName = "브라운";
    private final Long timeId = 1L;
    private final Long themeId = 1L;
    private int initialReservationSize;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        String sql = "SELECT COUNT(*) FROM reservation";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        initialReservationSize = Optional.ofNullable(result).orElse(0);
    }

    @Test
    void 예약_사용자_API() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        Long generatedId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        List<Long> allIds = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("id", Long.class);

        assertThat(allIds)
                .hasSize(initialReservationSize + 1)
                .contains(generatedId);

        JsonPath jsonPath = RestAssured.given().log().all()
                .when().get("/reservations?userName=" + userName)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath();

        List<Long> idsByUserName = jsonPath.getList("id", Long.class);
        List<String> names = jsonPath.getList("name", String.class);

        assertThat(idsByUserName)
                .hasSize(initialReservationSize + 1)
                .contains(generatedId);

        assertThat(names).containsOnly(userName);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + generatedId + "?userName=" + userName)
                .then().log().all()
                .statusCode(204);

        List<Long> remainIds = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("id", Long.class);

        assertThat(remainIds).hasSize(initialReservationSize);
        assertThat(remainIds).doesNotContain(generatedId);
    }

    @Test
    void 예약_사용자_시간_변경_API() {
        long id = 24L;
        Long timeId = 2L;
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        Long updatedTimeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().put("/reservations/" + id)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getLong("timeResponse.id");

        assertThat(updatedTimeId).isEqualTo(timeId);
    }

    @Test
    void 예약_사용자_날짜_변경_API() {
        long id = 24L;
        String date = "2026-05-13";
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        String updatedDate = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().put("/reservations/" + id)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getString("date");

        assertThat(updatedDate).isEqualTo(date);
    }

    @Test
    @DisplayName("사용자 이름이 null이면 상태코드 400을 반환한다.")
    void 요청_이름_null_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", FUTURE_DATE);
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
    @DisplayName("예약 날짜가 null이면 상태코드 400을 반환한다.")
    void 요청_날짜_null_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
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
    @DisplayName("예약 날짜의 형식이 올바르지 않으면 상태코드 400을 반환한다.")
    void 요청_날짜_형식_불일치_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
        params.put("date", "26-01-01");
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
    @DisplayName("시간 식별자가 null이면 상태코드 400을 반환한다.")
    void 요청_시간_식별자_null_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
        params.put("date", FUTURE_DATE);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("테마 식별자가 null이면 상태코드 400을 반환한다.")
    void 요청_테마_식별자_null_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
        params.put("date", FUTURE_DATE);
        params.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
