package roomescape.domain.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
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
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class AdminReservationControllerTest {

    @org.springframework.beans.factory.annotation.Value("${token}")
    private String adminToken;
    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private Long futureDateId;
    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        KeyHolder dateKeyHolder = new GeneratedKeyHolder();
        String futureDate = LocalDate.now().plusDays(10).toString();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_date(play_day) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, futureDate);
            return ps;
        }, dateKeyHolder);
        futureDateId = Objects.requireNonNull(dateKeyHolder.getKey()).longValue();

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
    @DisplayName("관리자 권한으로 모든 예약을 조회한다.")
    void getAllReservations() {
        jdbcTemplate.update("insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
            "관리자조회용", futureDateId, timeId, themeId);

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(200)
            .body("any { it.name == '관리자조회용' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 예약을 삭제한다.")
    void deleteReservation() {
        KeyHolder reservationKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "삭제될예약");
            ps.setLong(2, futureDateId);
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, reservationKeyHolder);
        Long reservationId = Objects.requireNonNull(reservationKeyHolder.getKey()).longValue();

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .when().delete("/admin/reservations/" + reservationId)
            .then().log().all()
            .statusCode(204);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하면 404 에러가 발생한다.")
    void deleteNotFoundReservation() {
        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .when().delete("/admin/reservations/" + 999)
            .then().log().all()
            .statusCode(404);
    }

    @Test
    @DisplayName("관리자 토큰 없이 접근할 경우 401 에러가 발생한다.")
    void unauthorizedAccess() {
        RestAssured.given().log().all()
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(401);
    }
}
