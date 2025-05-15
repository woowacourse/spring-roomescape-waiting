package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.ReservationController;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("""
                SET REFERENTIAL_INTEGRITY FALSE;
                TRUNCATE TABLE reservation;
                ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;
                TRUNCATE TABLE reservation_time;
                ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;
                TRUNCATE TABLE theme;
                ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;
                TRUNCATE TABLE member;
                ALTER TABLE member ALTER COLUMN id RESTART WITH 1;
                SET REFERENTIAL_INTEGRITY TRUE;
                """);
        memberRepository.save(new Member("name", "admin@naver.com", "1234", MemberRole.ADMIN));
    }

    @Test
    void 일단계_어드민_페이지_접근() {
        // given
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 이단계_어드민_예약페이지_접근() {
        // given
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // then
        // when
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 이단계_예약조회() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0)); // 아직 생성 요청이 없으니 Controller에서 임의로 넣어준 Reservation 갯수 만큼 검증하거나 0개임을 확인하세요.
    }

    @Test
    void 삼단계_유저_예약() {
        // given
        Map<String, String> params = new HashMap<>();
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(1);
        params.put("date", localDate.toString());
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 00)));
        themeRepository.save(Theme.of("name", "desc", "thumb"));
        params.put("timeId", "1");
        params.put("themeId", "1");
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 삼단계_delete_존재하지_않는_id_실패() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 사단계_db연결() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 오단계_예약_조회() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 00)));
        themeRepository.save(Theme.of("name", "desc", "thumb"));

        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");
        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES (?, ?, ?, ?, ?)",
                "2023-08-05", 1, 1, 1, "BOOKED");

        // when
        // then
        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);
        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    void 육단계_예약_삭제() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 00)));
        themeRepository.save(Theme.of("name", "desc", "thumb"));
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");
        Map<String, String> params = new HashMap<>();
        params.put("date", "2999-08-05");
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .cookie("token", token)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    void 칠단계_예약_시간_삭제() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 00)));

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 팔단계_예약_조회() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 00)));
        themeRepository.save(Theme.of("name", "desc", "thumb"));

        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2999-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .cookie("token", token)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        // when
        // then
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Autowired
    private ReservationController reservationController;

    @Test
    void 구단계() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
