package roomescape.controller.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.domain.member.Member;
import roomescape.global.JwtManager;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.reservation.ReservationTimeRequest;
import roomescape.service.dto.reservation.ReservationTimeResponse;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.time.AvailableTimeResponse;
import roomescape.service.dto.time.AvailableTimeResponses;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationTimeRestControllerTest {

    @LocalServerPort
    private int port;

    private final Member admin = new Member(2L, "t2@t2.com", "124", "재즈", "ADMIN");
    private String adminToken;

    @Autowired
    private JwtManager jwtManager;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        adminToken = jwtManager.generateToken(admin);
    }

    private final ReservationTimeRequest reservationTimeCreate1 = new ReservationTimeRequest("10:00");
    private final ReservationTimeRequest reservationTimeCreate2 = new ReservationTimeRequest("12:00");

    private final ThemeRequest themeCreate1 = new ThemeRequest("공포", "공포는 무서워", "hi.jpg");

    private final MemberCreateRequest memberCreate1 = new MemberCreateRequest("t1@t1.com", "123", "재즈");

    private final AdminReservationRequest reservationCreate1 = new AdminReservationRequest(1L, 1L,
            "2100-01-01", 1L);

    private void create(String path, Object param) {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(param)
                .when().post(path)
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 시간을 생성하는데 성공하면 응답과 201 상태 코드를 반환한다.")
    @Test
    void return_201_when_create_reservation_time() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationTimeCreate1)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 시간 목록을 조회하는데 성공하면 응답과 200 상태 코드를 반환한다.")
    @Test
    void return_200_when_find_all_reservation_times() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationTimeCreate1)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        List<ReservationTimeResponse> actualResponse = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationTimeResponse.class);

        ReservationTimeResponse expectedResponse = new ReservationTimeResponse(1L, "10:00");

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expectedResponse));
    }

    @DisplayName("예약 가능한 시간을 조회하는데 성공하면 응답과 200 상태 코드를 반환한다.")
    @Test
    void return_200_when_find_available_reservation_times() {
        create("/members/signup", memberCreate1);
        create("/admin/themes", themeCreate1);
        create("/admin/times", reservationTimeCreate1);
        create("/admin/times", reservationTimeCreate2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(reservationCreate1)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        AvailableTimeResponses actualResponse = RestAssured.given().log().all()
                .when().get("/times/available?date=2100-01-01&themeId=1")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", AvailableTimeResponses.class);

        AvailableTimeResponse availableTimeResponse1 = new AvailableTimeResponse(
                new ReservationTimeResponse(1L, "10:00"), true);
        AvailableTimeResponse availableTimeResponse2 = new AvailableTimeResponse(
                new ReservationTimeResponse(2L, "12:00"), false);
        AvailableTimeResponses expectedResponse = new AvailableTimeResponses(List.of(
                availableTimeResponse1, availableTimeResponse2
        ));

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @DisplayName("예약 시간을 삭제하는데 성공하면 응답과 204 상태 코드를 반환한다.")
    @Test
    void return_204_when_delete_reservation_time() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationTimeCreate1)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }
}
