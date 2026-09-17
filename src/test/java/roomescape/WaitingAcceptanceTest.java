package roomescape;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingAcceptanceTest {

    private static final String MEMBER_NAME_HEADER = "X-Member-Name";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약된 슬롯에 대기를 신청하면 순번과 함께 생성된다")
    void createWaiting() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("확정자");
        Map<String, Object> body = Map.of("themeSlotId", themeSlotId);

        RestAssured.given().log().all()
                .header(MEMBER_NAME_HEADER, "brown")
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("brown"))
                .body("status", equalTo("1번째 예약대기"))
                .body("waitingOrder", equalTo(1));
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 이미 예약이나 대기가 있으면 대기 신청을 거절한다")
    void rejectDuplicatedWaiting() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("brown");
        Map<String, Object> body = Map.of("themeSlotId", themeSlotId);

        RestAssured.given().log().all()
                .header(MEMBER_NAME_HEADER, "brown")
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("예약 API는 이미 예약된 슬롯에 새 대기 예약을 만들지 않고 거절한다")
    void rejectReservationWhenThemeSlotIsAlreadyReserved() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("confirmed-user");
        Map<String, Object> body = Map.of(
                "name", "brown",
                "themeSlotId", themeSlotId
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("대기 취소 후 내 예약 조회에서 대기 목록이 사라진다")
    void deleteWaiting() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("확정자");
        Number waitingId = RestAssured.given().log().all()
                .header(MEMBER_NAME_HEADER, "brown")
                .contentType(ContentType.JSON)
                .body(Map.of("themeSlotId", themeSlotId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .header(MEMBER_NAME_HEADER, "brown")
                .when().delete("/waitings/{id}", waitingId.longValue())
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header(MEMBER_NAME_HEADER, "brown")
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservationResponses", empty());
    }

    private long createReservedThemeSlotWithConfirmedReservation(String name) {
        long themeSlotId = insertThemeSlot(LocalDate.now().plusDays(30));
        jdbcTemplate.update("""
                        INSERT INTO reservation (name, status, theme_slot_id)
                        VALUES (?, 'CONFIRMED', ?)
                        """,
                name,
                themeSlotId
        );
        return themeSlotId;
    }

    private long insertThemeSlot(LocalDate date) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
                    VALUES (?, ?, ?, true)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, 4L);
            ps.setObject(2, date);
            ps.setLong(3, 6L);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
