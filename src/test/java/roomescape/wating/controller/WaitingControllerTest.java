package roomescape.wating.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Sql("/clear.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WaitingControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("테마 날짜 시간 예약자명으로 대기를 등록할 수 있다")
    void registerWaitingWithThemeDateTimeAndCustomerName() {
        //given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", futureDate, time.getId(), theme.getId());
        Map<String, String> body = Map.of(
                "name", "재키",
                "email", emailFromName("재키"),
                "date", futureDate.toString(),
                "timeId", time.getId().toString(),
                "themeId", theme.getId().toString()
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        response.then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("존재하지 않는 시간으로 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenRegisteringWaitingWithNonExistingTime() {
        //given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime unSavedTime = ReservationTime.of(999L, LocalTime.of(12, 00));
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
        Map<String, String> body = Map.of(
                "name", "재키",
                "email", emailFromName("재키"),
                "date", futureDate.toString(),
                "timeId", unSavedTime.getId().toString(),
                "themeId", theme.getId().toString()
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        response.then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", is("존재하지 않는 예약 시간입니다."));
    }

    @Test
    @DisplayName("존재하지 않는 테마로 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenRegisteringWaitingWithNonExistingTheme() {
        //given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = insertReservationTime("11:00:00");
        Theme unSavedTheme = Theme.of(999L, "name", "des", "url", 10000);
        Map<String, String> body = Map.of(
                "name", "재키",
                "email", emailFromName("재키"),
                "date", futureDate.toString(),
                "timeId", time.getId().toString(),
                "themeId", unSavedTheme.getId().toString()
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        response.then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", is("존재하지 않는 테마입니다."));
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복으로 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenSameCustomerRegistersDuplicateWaitingInSameSlot() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);

        final String customerName = "재키";
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        insertReservation("브라운", futureDate, time.getId(), theme.getId());
        insertWaiting(customerName, futureDate, time.getId(), theme.getId());

        Map<String, String> body = Map.of(
                "name", customerName,
                "email", emailFromName(customerName),
                "date", futureDate.toString(),
                "timeId", time.getId().toString(),
                "themeId", theme.getId().toString()
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        response.then().log().all()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", is("해당 시간에 이미 대기가 존재합니다."));
    }

    @Test
    @DisplayName("대기 아이디와 본인의 이름으로 대기를 삭제할 수 있다")
    void deleteWaitingByIdAndOwnName() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);

        final String customerName = "재키";
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long savedWaitingId = insertWaiting(customerName, futureDate, time.getId(), theme.getId());

        //when
        final Response response = RestAssured.given().log().all()
                .queryParam("customer-name", customerName)
                .queryParam("customer-email", emailFromName(customerName))
                .when().delete("/waitings/{id}", savedWaitingId);

        //then
        response.then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("본인 이름으로 등록되지 않은 대기를 삭제하려 하면 404를 반환한다")
    void respondNotFoundWhenDeletingWaitingNotOwnedByName() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);

        final String customerName = "코로구";
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long savedWaitingId = insertWaiting(customerName, futureDate, time.getId(), theme.getId());

        //when
        final String invalidCustomerName = "재키";
        final Response response = RestAssured.given().log().all()
                .queryParam("customer-name", invalidCustomerName)
                .queryParam("customer-email", emailFromName(invalidCustomerName))
                .when().delete("/waitings/{id}", savedWaitingId);

        //then
        response.then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", is("존재하지 않는 대기입니다."));
    }

    @Test
    @DisplayName("존재하지 않는 대기를 삭제하려 하면 404를 반환한다")
    void respondNotFoundWhenDeletingNonExistingWaiting() {
        //given
        final String customerName = "코로구";
        final long unsavedWaitingId = 999L;

        //when
        final Response response = RestAssured.given().log().all()
                .queryParam("customer-name", customerName)
                .queryParam("customer-email", emailFromName(customerName))
                .when().delete("/waitings/{id}", unsavedWaitingId);

        //then
        response.then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", is("존재하지 않는 대기입니다."));
    }

    @Test
    @DisplayName("과거 날짜의 대기를 삭제하는 경우 422를 반환한다")
    void respondUnprocessableEntityWhenDeletingPastWaiting() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);

        final String customerName = "재키";
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        final long savedWaitingId = insertWaiting(customerName, yesterday, time.getId(), theme.getId());

        //when
        final Response response = RestAssured.given().log().all()
                .queryParam("customer-name", customerName)
                .queryParam("customer-email", emailFromName(customerName))
                .when().delete("/waitings/{id}", savedWaitingId);

        //then
        response.then().log().all()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .body("message", is("과거 시간 예약의 대기를 삭제할 수 없습니다."));
    }

    private ReservationTime insertReservationTime(final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                startAt
        );
        return ReservationTime.of(1L, Time.valueOf(startAt).toLocalTime());
    }

    private Theme insertTheme(
            final String name,
            final String description,
            final String thumbnailUrl,
            final int price
    ) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                name,
                description,
                thumbnailUrl,
                price
        );

        return Theme.of(1L, name, description, thumbnailUrl, price);
    }

    private Long insertReservation(
            final String name,
            final LocalDate date,
            final long timeId,
            final long themeId
    ) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation(customer_name, customer_email, slot_id, status) VALUES (?, ?, ?, ?)",
                name,
                emailFromName(name),
                slotId,
                "CONFIRMED"
        );
        return slotId;
    }

    private long insertWaiting(
            final String name,
            final LocalDate reservationDate,
            final long timeId,
            final long themeId
    ) {
        Long slotId = insertReservationSlot(reservationDate, timeId, themeId);
        final String sql = """
                INSERT INTO waiting(customer_name, customer_email, slot_id)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, name);
            ps.setString(2, emailFromName(name));
            ps.setLong(3, slotId);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("대기 생성에 실패했습니다.");
        }
        return key.longValue();
    }

    private String emailFromName(final String name) {
        return "customer" + Math.abs(name.hashCode()) + "@example.com";
    }

    private Long insertReservationSlot(final LocalDate reservationDate, final long timeId, final long themeId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO reservation_slot(reservation_date, time_id, theme_id) VALUES (?, ?, ?)",
                    Date.valueOf(reservationDate),
                    timeId,
                    themeId
            );
        } catch (DuplicateKeyException ignored) {
        }
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE reservation_date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                Date.valueOf(reservationDate),
                timeId,
                themeId
        );
    }
}
