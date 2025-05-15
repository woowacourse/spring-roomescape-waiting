package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.RESERVATION_BODY;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.ReservationDto;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.TimeRepository;
import roomescape.infrastructure.jwt.JwtTokenProvider;
import roomescape.presentation.controller.ReservationController;
import roomescape.testFixture.JdbcHelper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MissionStepTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String tokenForAdmin;

    @BeforeEach
    void cleanDatabase() {
        RestAssured.port = port;

        resetH2TableIds(jdbcTemplate);
        tokenForAdmin = jwtTokenProvider.createToken(new MemberIdDto(MEMBER1_ADMIN.getId()));
    }

    @DisplayName("/ 요청 시 200 OK 반환")
    @Test
    void welcomePage_redirect_to_reservationPage() {
        RestAssured.given().log().all()
                .redirects().follow(false) // 리디렉션 따라가지 말고 그대로 응답 확인
                .when().get("/")
                .then().log().all()
                .statusCode(200); // 또는 301, 실제 리디렉션 코드에 따라 다름
    }

    @DisplayName("/admin 요청 시 200 OK 응답")
    @Test
    void request_adminPage_then_200() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);

        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("1단계 - /admin/reservation 요청 시 200 OK")
    @Test
    void request_ReservationAdminPage_then_200() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);

        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .when()
                .get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("2단계 - 모든 예약을 가져오는 api 호출 시, 현재 저장소의 예약 개수와 일치해야한다.")
    @Test
    void request_getAllReservations() {
        int reservationsCount = reservationRepository.findAll().size();

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(reservationsCount));
    }

    @DisplayName("3단계 - 예약 추가 api 호출 시, id가 정상적으로 부여된다.")
    @Test
    public void request_addReservation() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                1L, "10:00");

        int repositorySize = reservationRepository.findAll().size();
        int expectedSize = repositorySize + 1;

        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .contentType(ContentType.JSON)
                .body(RESERVATION_BODY)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(expectedSize));

        int afterAddSize = reservationRepository.findAll().size();
        assertThat(afterAddSize).isEqualTo(expectedSize);
    }

    @DisplayName("3단계 - id로 예약을 삭제할 수 있다.")
    @Test
    void requestDeleteReservation() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                1L, "10:00");

        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                1L, "2023-08-05", 1L, 1L
        );
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("4단계 - 데이터베이스 연결 성공")
    @Test
    void connectionTest() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("5단계 - 데이터베이스에 예약 추가 및 조회 성공")
    @Test
    void postAndGetReservation() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                1L, "10:00");

        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                1L, "2023-08-05", 1L, 1L
        );

        List<ReservationDto> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationDto.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @DisplayName("6단계 - 데이터베이스에 예약 추가 성공")
    @Test
    void postAndDeleteReservation() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);

        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                1L, "10:00");

        int beforeCount = reservationRepository.findAll().size();

        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .contentType(ContentType.JSON)
                .body(RESERVATION_BODY)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(beforeCount + 1);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(beforeCount);
    }

    @DisplayName("7단계 - 예약 시간 추가 및 삭제 성공")
    @Test
    void reservationTimeTest() {
        int beforeSize = timeRepository.findAll().size();

        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(beforeSize + 1));

        RestAssured.given().log().all()
                .when().delete(String.format("/times/%d", beforeSize + 1))
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("8단계 - 시간을 선택해서 예약 추가 및 조회 성공")
    @Test
    void postAndGetReservationWithTime() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                1L, "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", tokenForAdmin)
                .body(RESERVATION_BODY)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("9단계 - 데이터베이스 로직 분리 테스트")
    @Test
    void layeredArchitectureTest() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }

    @DisplayName("예약이 존재하는 시간은 삭제 불가")
    @Test
    void cannotDeleteTime_when_hasReservation() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')");
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) VALUES (1, '테마1', '테마1입니다.', '썸네일')");
        jdbcTemplate.update(
                "INSERT INTO reservation (id, member_id, date, time_id, theme_id) VALUES (1, 1, '2025-01-01', 1, 1)");

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("존재하지 않는 테마 id로 예약 생성 시 예외 발생")
    @Test
    void error_when_id_notFound() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);

        Long timeId = 999L;
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("member_id", 1);
        String date = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", tokenForAdmin)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("중복된 일시로 예약 생성 시 예외 발생")
    @Test
    void error_when_duplicateReservation() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')");
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) VALUES (1, '테마1', '테마1입니다.', '썸네일')");
        jdbcTemplate.update(
                "INSERT INTO reservation (id, member_id, date, time_id, theme_id) VALUES (1, 1, ?, ?, 1)",
                date, timeId);

        // when
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", date);
        reservation.put("timeId", timeId);

        // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("/admin/theme 요청 시 200 OK 응답")
    @Test
    void request_adminThemePage_then_200() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);

        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }
}
