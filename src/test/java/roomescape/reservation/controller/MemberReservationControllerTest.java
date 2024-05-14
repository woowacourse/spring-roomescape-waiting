package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.controller.dto.request.LoginRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.SelectableTimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberReservationControllerTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    String token;

    @BeforeEach
    void setUp() {
        // login
        token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("parang@gmail.com", "password"))
                .when().post("/login")
                .cookie("token");
    }

    @DisplayName("예약을 정상적으로 추가한다.")
    @Test
    void save() {
        // when
        Map<String, Object> reservations = new HashMap<>();
        reservations.put("date", LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        reservations.put("timeId", 1);
        reservations.put("themeId", 1);
        reservations.put("memberId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .cookie("token", token)
                .when().post("/reservations")
                .then().statusCode(201);

        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }

    @DisplayName("동일한 날짜, 시간, 테마에 예약 내역이 이미 있다면 예약할 수 없다.")
    @Test
    void cannotSaveDuplicatedReservation() {
        // given
        Map<String, Object> reservations = new HashMap<>();
        reservations.put("date", "2024-12-12");
        reservations.put("timeId", 1);
        reservations.put("themeId", 1);
        reservations.put("memberId", 1);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .cookie("token", token)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약하려는 시간이 이전 시간보다 이전일 경우 예약할 수 없다.")
    @Test
    void cannotSaveBeforeNowReservation() {
        // given
        Map<String, Object> reservations = new HashMap<>();
        reservations.put("member_name", "브라운");
        reservations.put("date", LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        reservations.put("timeId", 1);
        reservations.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .cookie("token", token)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약 리스트를 조회한다.")
    @Test
    void getAll() {
        // when & then
        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        assertThat(reservations).hasSize(count);
    }

    @DisplayName("예약 가능한 시간을 조회한다.")
    @Test
    void findSelectableTimes() {
        // when & then
        List<SelectableTimeResponse> responses = RestAssured.given().log().all()
                .param("date", LocalDate.now().plusYears(1))
                .param("themeId", 1)
                .when().get("/times")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", SelectableTimeResponse.class);
        assertThat(responses).hasSize(4);
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void delete() {
        // when
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(200);

        // then
        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(2);
    }
}
