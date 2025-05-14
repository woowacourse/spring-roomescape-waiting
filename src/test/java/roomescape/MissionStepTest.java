package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dao.ReservationTimeJdbcDao;
import roomescape.dao.ThemeJdbcDao;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.service.JwtProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeJdbcDao reservationTimeDao;

    @Autowired
    private ThemeJdbcDao themeDao;

    private String email;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void beforeEachTest() {
        reservationTimeDao.saveTime(new ReservationTime(LocalTime.of(10, 10)));
        themeDao.saveTheme(new Theme("공포", "무서워요", "image"));

        this.email = "email@gmail.com";
        jdbcTemplate.update("INSERT INTO member"
                        + " (name, email,password, role) VALUES (?, ?, ?, ?)"
                , "히로", email, "password", Role.ADMIN.name());
    }

    @DisplayName("관리자 페이지 GET 요청 시 200 OK를 반환한다")
    @Test
    void 일단계() {
        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("GET /admin/reservation 및 /reservations 요청 시 200 OK와 빈 목록 반환 확인")
    @Test
    void 이단계() {
        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("예약 생성 후 조회 및 삭제까지의 전체 흐름 테스트")
    @Test
    void 삼단계() {
        Map<String, String> params = new HashMap<>();
        params.put("date", String.valueOf(LocalDate.now().plusDays(1)));
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .cookie("token", createToken())
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("DB 연결 및 RESERVATION 테이블 존재 여부 확인")
    @Test
    void 사단계() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("조회한 예약 수와 데이터베이스 쿼리를 통해 조회한 예약 수가 같은지 비교하는 테스트")
    @Test
    void 오단계() {
        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)",
                String.valueOf(LocalDate.now().plusDays(1)), "1", "1", "1");

        List<ReservationResponseDto> reservations = RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponseDto.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @DisplayName("예약 추가 및 삭제 테스트")
    @Test
    void 육단계() {
        Map<String, String> params = new HashMap<>();
        params.put("date", String.valueOf(LocalDate.now().plusDays(1)));
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .cookie("token", createToken())
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @DisplayName("상영시간을 등록하고 조회하면 목록에 포함되며 삭제도 가능하다")
    @Test
    void 칠단계() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "13:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예약 추가 이후 예약 개수 확인 테스트")
    @Test
    void 팔단계() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", String.valueOf(LocalDate.now().plusDays(1)));
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", createToken())
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", createToken())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    private String createToken() {
        return jwtProvider.createToken(email);
    }
}
