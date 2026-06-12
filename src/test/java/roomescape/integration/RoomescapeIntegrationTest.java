package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    void 에러_응답_규격_검증() {
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
                .body("detail", equalTo("지난 날짜/시간으로 예약하실 수 없습니다."))
                .body("code", notNullValue());
    }

    @Test
    @DisplayName("특정 사용자의 이름으로 본인의 예약과 대기 목록만 조회할 수 있다.")
    void 내_예약_대기_조회() {
        insertTestData();

        RestAssured.given().log().all()
                .queryParam("userName", "브라운")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservationAndWaitingResponses.size()", equalTo(1))
                .body("reservationAndWaitingResponses[0].name", equalTo("브라운"));
    }

    @Test
    @DisplayName("날짜와 테마를 선택하면 해당 조건에 맞는 예약 가능한 시간 목록이 표시된다.")
    void 예약_가능_시간_조회() {
        insertTestData();
        String dateStr = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

        RestAssured.given().log().all()
                .queryParam("themeId", 1)
                .queryParam("date", dateStr)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("timeResponses.find { it.id == 1 }.isAvailable", equalTo(false))
                .body("timeResponses.find { it.id == 2 }.isAvailable", equalTo(true));
    }

    @Test
    @DisplayName("같은 날짜와 시간이라도 테마가 다르면 각각 예약에 성공한다.")
    void 테마별_독립_예약_검증() {
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
    void 인기_테마_기간_경계_검증() {
        insertTestData();
        LocalDate today = LocalDate.now();
        insertReservation("유저1", today.minusDays(8), today.minusDays(9).atStartOfDay(), 1L, 1L);
        insertReservation("유저2", today.minusDays(3), today.minusDays(9).atStartOfDay(), 1L, 2L);
        insertReservation("유저3", today.plusDays(2), today.minusDays(9).atStartOfDay(), 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("limit", 10)
                .queryParam("days", 7)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("themeResponses.size()", greaterThanOrEqualTo(1))
                .body("themeResponses[0].id", equalTo(2));
    }

    @Test
    @DisplayName("예약자가 예약을 취소하면 첫 번째 대기가 예약으로 자동 승급된다.")
    void 예약_취소시_첫번째_대기_자동_승급() {
        LocalDate tomorrow = LocalDate.now().plusDays(2);
        insertTestData(tomorrow);

        ReservationRequest firstWaitingRequest = new ReservationRequest("네오", tomorrow, 1L, 1L);
        ReservationRequest secondWaitingRequest = new ReservationRequest("대길", tomorrow, 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(firstWaitingRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo("WAITING"));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(secondWaitingRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo("WAITING"));

        RestAssured.given().log().all()
                .queryParam("userName", "브라운")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("userName", "네오")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservationAndWaitingResponses.size()", equalTo(1))
                .body("reservationAndWaitingResponses[0].name", equalTo("네오"))
                .body("reservationAndWaitingResponses[0].isReserved", equalTo(true))
                .body("reservationAndWaitingResponses[0].waitingNumber", equalTo(null));

        RestAssured.given().log().all()
                .queryParam("userName", "대길")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservationAndWaitingResponses.size()", equalTo(1))
                .body("reservationAndWaitingResponses[0].name", equalTo("대길"))
                .body("reservationAndWaitingResponses[0].isReserved", equalTo(false))
                .body("reservationAndWaitingResponses[0].waitingNumber", equalTo(1));
    }

    private void insertTestData() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        insertTestData(tomorrow);
    }

    private void insertTestData(LocalDate tomorrow) {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마1", "설명1", "thumbnail1.png");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마2", "설명2", "thumbnail2.png");
        jdbcTemplate.update("INSERT INTO time_slot (start_at) VALUES (?)", "10:00:00");
        jdbcTemplate.update("INSERT INTO time_slot (start_at) VALUES (?)", "11:00:00");
        insertReservation("브라운", tomorrow, LocalDate.now().atStartOfDay(), 1L, 1L);
    }

    private void insertReservation(String name, LocalDate date, LocalDateTime createdAt, Long timeId, Long themeId) {
        jdbcTemplate.update("INSERT INTO reservation_slot (date, time_id, theme_id) VALUES (?, ?, ?)",
                date, timeId, themeId);
        Long slotId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                timeId,
                themeId
        );
        jdbcTemplate.update("INSERT INTO reservation (name, slot_id, created_at, status) VALUES (?, ?, ?, ?)",
                name, slotId, createdAt, "RESERVED");
    }
}
