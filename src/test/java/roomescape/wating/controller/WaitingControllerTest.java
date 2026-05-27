package roomescape.wating.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.sql.Date;
import java.sql.PreparedStatement;
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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.wating.domain.exception.PastReservationWaitingCancellationException;
import roomescape.wating.domain.exception.WaitingNotFoundException;
import roomescape.wating.domain.exception.WaitingSlotDuplicateException;

@Sql("/clear.sql")
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
        final ReservationTime time = insertReservationTime("11:00:00");
        final Theme theme = insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", LocalDate.of(2026, 5, 26), time.getId(), theme.getId());

        final Map<String, String> body = Map.of(
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
        final ReservationTime unSavedTime = ReservationTime.of(999L, LocalTime.of(12, 00));
        final Theme theme = insertTheme("링", "공포 테마", "http:~");

        final Map<String, String> body = Map.of(
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
        final ReservationTime time = insertReservationTime("11:00:00");
        final Theme unSavedTheme = Theme.of(999L, "name", "des", "url");

        final Map<String, String> body = Map.of(
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
        final ReservationTime time = insertReservationTime("11:00:00");
        final Theme theme = insertTheme("링", "공포 테마", "http:~");

        final String customerName = "재키";
        final LocalDate nowDate = NOW.toLocalDate();
        insertReservation("브라운", nowDate, time.getId(), theme.getId());
        insertWaiting(customerName, nowDate, time.getId(), theme.getId());

        final Map<String, String> body = Map.of(
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

    @Test
    void 대기_아이디와_본인의_이름으로_대기를_삭제할_수_있다() {
        //given
        final ReservationTime time = insertReservationTime("11:00:00");
        final Theme theme = insertTheme("링", "공포 테마", "http:~");

        final String customerName = "재키";
        final LocalDate nowDate = NOW.toLocalDate();
        final long savedWaitingId = insertWaiting(customerName, nowDate, time.getId(), theme.getId());

        //when
        final Response response = RestAssured.given().log().all()
            .queryParam("customer-name", customerName)
            .when().delete("/waitings/{id}", savedWaitingId);

        //then
        response.then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 본인_이름으로_등록되지_않은_대기를_삭제하려_하면_404를_반환한다() {
        //given
        final ReservationTime time = insertReservationTime("11:00:00");
        final Theme theme = insertTheme("링", "공포 테마", "http:~");

        final String customerName = "코로구";
        final LocalDate nowDate = NOW.toLocalDate();
        final long savedWaitingId = insertWaiting(customerName, nowDate, time.getId(), theme.getId());

        //when
        final String invalidCustomerName = "재키";
        final Response response = RestAssured.given().log().all()
            .queryParam("customer-name", invalidCustomerName)
            .when().delete("/waitings/{id}", savedWaitingId);

        //then
        final Exception expectedException = new WaitingNotFoundException();
        response.then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("message", is(expectedException.getMessage()));
    }

    @Test
    void 존재하지_않는_대기를_삭제하려_하면_404를_반환한다() {
        //given
        final String customerName = "코로구";
        final long unsavedWaitingId = 999L;

        //when
        final Response response = RestAssured.given().log().all()
            .queryParam("customer-name", customerName)
            .when().delete("/waitings/{id}", unsavedWaitingId);

        //then
        final Exception expectedException = new WaitingNotFoundException();
        response.then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("message", is(expectedException.getMessage()));
    }

    @Test
    void 과거_날짜의_대기를_삭제하는_경우_422를_반환한다() {
        //given
        final ReservationTime time = insertReservationTime("11:00:00");
        final Theme theme = insertTheme("링", "공포 테마", "http:~");

        final String customerName = "재키";
        final LocalDate yesterday = NOW.minusDays(1).toLocalDate();
        final long savedWaitingId = insertWaiting(customerName, yesterday, time.getId(), theme.getId());

        //when
        final Response response = RestAssured.given().log().all()
            .queryParam("customer-name", customerName)
            .when().delete("/waitings/{id}", savedWaitingId);

        //then
        final Exception expectedException = new PastReservationWaitingCancellationException();
        response.then().log().all()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
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

    private void insertReservation(
        final String name,
        final LocalDate date,
        final long timeId,
        final long themeId
    ) {
        jdbcTemplate.update(
            "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            name,
            Date.valueOf(date),
            timeId,
            themeId
        );
    }

    private long insertWaiting(
        final String name,
        final LocalDate reservationDate,
        final long timeId,
        final long themeId
    ) {
        final String sql = """
            INSERT INTO waiting(customer_name, reservation_date, time_id, theme_id)
            VALUES (?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(reservationDate));
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
