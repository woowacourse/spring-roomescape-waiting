package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.dto.ReservationRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RoomescapeIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("ProblemDetail 에러 응답 규격을 정확히 준수하여 반환한다.")
    void problemDetailFormatTest() {
        insertTestData();
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now().minusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("title", notNullValue())
                .body("status", equalTo(400))
                .body("detail", equalTo("지난 날짜로 예약하실 수 없습니다."))
                .body("code", notNullValue());
    }

    @Test
    @DisplayName("특정 사용자의 이름으로 본인의 예약과 대기 목록만 조회할 수 있다.")
    void getMyReservations() {
        insertTestData();

        RestAssured.given().log().all()
                .queryParam("userName", "브라운")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].name", equalTo("브라운"));
    }

    @Test
    @DisplayName("날짜와 테마를 선택하면 해당 조건에 맞는 예약 가능한 시간 목록이 표시된다.")
    void fetchAvailableTimes() {
        insertTestData();
        String dateStr = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

        RestAssured.given().log().all()
                .queryParam("themeId", 1)
                .queryParam("date", dateStr)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("find { it.id == 1 }.isAvailable", equalTo(false))
                .body("find { it.id == 2 }.isAvailable", equalTo(true));
    }

    @Test
    @DisplayName("같은 날짜와 시간이라도 테마가 다르면 각각 예약에 성공한다.")
    void independentThemeReservation() {
        insertTestData();

        ReservationRequest request = new ReservationRequest("네오", LocalDate.now().plusDays(1), 1L, 2L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("최근 7일(기간 파라미터) 동안의 예약 데이터를 기반으로 인기 테마 상위 목록을 조회한다.")
    void getPopularThemesBoundary() {
        insertTestData();
        LocalDate today = LocalDate.now();
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "유저1",
                today.minusDays(8), 1L, 1L);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "유저2",
                today.minusDays(3), 1L, 2L);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "유저3",
                today.plusDays(2), 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("topCount", 10)
                .queryParam("during", 7)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].id", equalTo(2));
    }

    private void insertTestData() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', ?, 1, 1)",
                tomorrow);
    }
}
