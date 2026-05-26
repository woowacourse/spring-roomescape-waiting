package roomescape.wating.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.wating.domain.exception.WaitingSlotDuplicateException;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WaitingControllerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDateTime NOW = LocalDateTime.now(FIXED_CLOCK);


    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    int port;

    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return FIXED_CLOCK;
        }
    }

    @Test
    void 테마_날짜_시간_예약자명으로_대기를_등록할_수_있다() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~");
        Map<String, String> body = Map.of(
                "name", "재키",
                "date", "2026-05-26",
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
    void 존재하지_않는_시간으로_대기를_등록하면_예외가_발생한다() {
        //given
        ReservationTime unSavedTime = ReservationTime.of(999L, LocalTime.of(12, 00));
        Theme theme = insertTheme("링", "공포 테마", "http:~");
        Map<String, String> body = Map.of(
                "name", "재키",
                "date", "2026-05-26",
                "timeId", unSavedTime.getId().toString(),
                "themeId", theme.getId().toString()
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        final Exception expectedException = new ReservationTimeNotFoundException();
        response.then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", is(expectedException.getMessage()));
    }

    @Test
    void 존재하지_않는_테마로_대기를_등록하면_예외가_발생한다() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme unSavedTheme = Theme.of(999L, "name", "des", "url");
        Map<String, String> body = Map.of(
                "name", "재키",
                "date", "2026-05-26",
                "timeId", time.getId().toString(),
                "themeId", unSavedTheme.getId().toString()
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        final Exception expectedException = new ThemeNotFoundException();
        response.then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", is(expectedException.getMessage()));
    }

    @Test
    void 같은_사용자가_같은_슬롯에_중복으로_대기를_등록하면_예외가_발생한다() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~");

        final String customerName = "재키";
        final LocalDate nowDate = NOW.toLocalDate();
        insertWaiting(customerName, nowDate, time.getId(), theme.getId());

        Map<String, String> body = Map.of(
                "name", customerName,
                "date", nowDate.toString(),
                "timeId", time.getId().toString(),
                "themeId", theme.getId().toString()
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        final Exception expectedException = new WaitingSlotDuplicateException();
        response.then().log().all()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", is(expectedException.getMessage()));
    }

    private ReservationTime insertReservationTime(final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                startAt
        );
        return ReservationTime.of(1L, Time.valueOf(startAt).toLocalTime());
    }

    private Theme insertTheme(final String name, final String description, final String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name,
                description,
                thumbnailUrl
        );

        return Theme.of(1L, name, description, thumbnailUrl);
    }

    private void insertWaiting(
            final String name,
            final LocalDate reservationDate,
            final long timeId,
            final long themeId
    ) {
        jdbcTemplate.update("""
                        INSERT INTO waiting(customer_name, reservation_date, time_id, theme_id)
                        VALUES (?, ?, ?, ?)
                        """,
                name,
                reservationDate,
                timeId,
                themeId
        );
    }
}
