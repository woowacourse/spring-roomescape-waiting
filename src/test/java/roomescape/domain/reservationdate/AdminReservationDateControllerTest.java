package roomescape.domain.reservationdate;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql("/truncate.sql")
class AdminReservationDateControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @org.springframework.beans.factory.annotation.Value("${token}")
    private String adminToken;
    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("관리자 권한으로 예약 날짜를 생성한다.")
    void createReservationDate() {
        String farFutureDate = LocalDate.now().plusYears(10).toString();
        Map<String, Object> params = new HashMap<>();
        params.put("playDay", farFutureDate);

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .contentType("application/json")
            .body(params)
            .when().post("/admin/reservation-dates")
            .then().log().all()
            .statusCode(201)
            .body("playDay", is(farFutureDate));
    }

    @Test
    @DisplayName("관리자 권한으로 모든 예약 날짜를 조회한다.")
    void getAllReservationDateForAdmin() {
        String farFutureDate = LocalDate.now().plusYears(10).toString();
        jdbcTemplate.update("insert into reservation_date(play_day) values (?)", farFutureDate);

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .when().get("/admin/reservation-dates")
            .then().log().all()
            .statusCode(200)
            .body("any { it.playDay == '" + farFutureDate + "' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 날짜를 삭제한다.")
    void deleteReservationDate() {
        String farFutureDate = LocalDate.now().plusYears(10).toString();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_date(play_day) values (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, farFutureDate);
            return ps;
        }, keyHolder);
        Long dateId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        RestAssured.given().log().all()
            .header(ADMIN_HEADER, adminToken)
            .when().delete("/admin/reservation-dates/" + dateId)
            .then().log().all()
            .statusCode(204);
    }
}
