package roomescape.domain.reservation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private JpaReservationRepository reservationRepository;

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            ZoneId zoneId = ZoneId.systemDefault();
            return Clock.fixed(
                LocalDateTime.of(2026, 5, 31, 13, 0)
                    .atZone(zoneId)
                    .toInstant(),
                zoneId
            );
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM reservation_date");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("예약 생성을 end-to-end로 확인한다.")
    void createReservation() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");

        String request = """
            {
                "name": "보예",
                "dateId": %d,
                "timeId": %d,
                "themeId": %d
            }
            """.formatted(dateId, timeId, themeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("date", is("2026-06-01"))
            .body("time", is("10:00"))
            .body("theme.name", is("공포"))
            .body("theme.content", is("무서운 테마"))
            .body("theme.url", is("theme-url"));

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("username", is("보예"))
            .body("reservations", hasSize(1));
    }

    @Test
    @DisplayName("예약 생성 시 시간 필드가 누락되었을 경우 400 에러가 발생한다.")
    void createReservationWithoutTimeId() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");

        String request = """
            {
                "name": "보예",
                "dateId": %d,
                "themeId": %d
            }
            """.formatted(dateId, themeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("code", is("INPUT_VALIDATION_ERROR"))
            .body("message", is("시간은 필수 선택 사항 입니다. 시간을 선택해주세요."));
    }

    @Test
    @DisplayName("예약자 이름으로 예약 조회를 end-to-end로 확인한다.")
    void getUserReservations() {
        saveReservation("보예", "2026-06-01", "10:00", "공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("username", is("보예"))
            .body("reservations[0].reservationSlot.date.startWhen", is("2026-06-01"))
            .body("reservations[0].reservationSlot.time.startAt", is("10:00"))
            .body("reservations[0].reservationSlot.theme.name", is("공포"))
            .body("reservations[0].status", is("CONFIRMED"));
    }

    @Test
    @DisplayName("예약 조회 시 이름 파라미터가 누락되었을 경우 400 에러가 발생한다.")
    void getUserReservationsWithoutName() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("code", is("REQUIRED_PARAMETER_MISSING"))
            .body("message", is("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("예약 삭제를 end-to-end로 확인한다.")
    void deleteUserReservation() {
        Long reservationId = saveReservation("보예", "2026-06-01", "10:00", "공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .when().delete("/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("username", is("보예"))
            .body("reservations", hasSize(1))
            .body("reservations[0].status", is("CANCELED"))
            .body("reservations[0].waitingNumber", nullValue());
    }

    @Test
    @DisplayName("예약 취소 후 대기 전환 중 예외가 발생하면 예약 취소도 롤백된다.")
    void checkRollback() {
        // given
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");
        Long reservationSlotId = saveReservationSlot(dateId, timeId, themeId);
        Long confirmedReservationId = saveReservation(
            "보예",
            reservationSlotId,
            ReservationStatus.CONFIRMED
        );
        Long waitingReservationId = saveReservation(
            "수민",
            reservationSlotId,
            ReservationStatus.WAITING
        );
        doAnswer(invocationOnMock -> {
                Reservation reservation = invocationOnMock.getArgument(0);
                if (reservation.getId().equals(waitingReservationId)) {
                    throw new IllegalArgumentException("대기 전환에 실패했습니다.");
                }
                return invocationOnMock.callRealMethod();
            }
        ).when(reservationRepository).save(any(Reservation.class));

        // when & then
        given().
            contentType(ContentType.JSON)
            .when().delete("/reservations/{id}", confirmedReservationId)
            .then()
            .statusCode(500);

        assertSoftly(softly -> {
                assertThat(findReservationStatus(confirmedReservationId)).isEqualTo(ReservationStatus.CONFIRMED.toString());
                assertThat(findReservationStatus(waitingReservationId)).isEqualTo(ReservationStatus.WAITING.toString());
            }
        );
    }

    @Test
    @DisplayName("예약 수정을 end-to-end로 확인한다.")
    void updateReservation() {
        Long reservationId = saveReservation("보예", "2026-06-01", "10:00", "공포");
        Long dateId = saveDate("2026-06-02");
        Long timeId = saveTime("11:00");

        String request = """
            {
                "dateId": %d,
                "timeId": %d
            }
            """.formatted(dateId, timeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().patch("/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("reservations[0].reservationSlot.date.startWhen", is("2026-06-02"))
            .body("reservations[0].reservationSlot.time.startAt", is("11:00"));
    }

    private Long saveReservation(String name, String date, String time, String themeName) {
        Long themeId = saveTheme(themeName);
        Long dateId = saveDate(date);
        Long timeId = saveTime(time);

        Long reservationSlotId = saveReservationSlot(dateId, timeId, themeId);
        return saveReservation(name, reservationSlotId, ReservationStatus.CONFIRMED);
    }

    private Long saveReservation(String name, Long reservationSlotId, ReservationStatus status) {
        jdbcTemplate.update("INSERT INTO users(name) VALUES (?)", name);
        Long userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE name = ?",
            Long.class,
            name
        );
        jdbcTemplate.update(
            "INSERT INTO reservation(user_id, reservation_slot_id, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            userId,
            reservationSlotId,
            status.name()
        );

        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation WHERE user_id = ? AND reservation_slot_id = ?",
            Long.class,
            userId,
            reservationSlotId
        );
    }

    private Long saveReservationSlot(Long dateId, Long timeId, Long themeId) {
        jdbcTemplate.update(
            "INSERT INTO reservation_slot(date_id, time_id, theme_id) VALUES (?, ?, ?)",
            dateId,
            timeId,
            themeId
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_slot WHERE date_id = ? AND time_id = ? AND theme_id = ?",
            Long.class,
            dateId,
            timeId,
            themeId
        );
    }

    private String findReservationStatus(Long reservationId) {
        return jdbcTemplate.queryForObject(
            "SELECT status FROM reservation WHERE id = ?",
            String.class,
            reservationId
        );
    }

    private Long saveTheme(String themeName) {
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            themeName,
            "무서운 테마",
            "theme-url"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM theme WHERE name = ?",
            Long.class,
            themeName
        );
    }

    private Long saveDate(String date) {
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            date
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_date WHERE date = ?",
            Long.class,
            date
        );
    }

    private Long saveTime(String time) {
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            time
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            time + ":00"
        );
    }
}
