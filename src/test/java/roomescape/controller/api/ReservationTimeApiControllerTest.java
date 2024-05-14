package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.util.DatabaseCleaner;

//@formatter:off
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationTimeApiControllerTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeFixture reservationTimeFixture;

    @Autowired
    MemberService memberService;
    @Autowired
    ThemeService themeService;

    @LocalServerPort
    int port;

    @Autowired
    DatabaseCleaner databaseCleaner;
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.initialize();
    }


    @Test
    @DisplayName("시간 생성에 성공하면, 201을 반환한다")
    void return_201_when_reservationTime_create_success() {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().contentType(ContentType.JSON).body(params)
                   .when().post("/times")
                   .then().statusCode(201);

        RestAssured.given()
                   .when().get("/times")
                   .then().statusCode(200).body("size()", is(1));
    }

    @Test
    @DisplayName("시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때 400을 반환한다.")
    void return_400_when_reservationTime_create_input_is_invalid() {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "");

        RestAssured.given().contentType(ContentType.JSON).body(params)
                   .when().post("/times").then().statusCode(400);
    }

    @Test
    @DisplayName("특정 시간이 존재하지 않는데, 그 시간을 삭제하려 할 때 404을 반환한다.")
    void return_404_when_not_exist_id() {
        RestAssured.given()
                   .when().delete("/times/-1")
                   .then().statusCode(404);
    }

    @Test
    @DisplayName("특정 시간에 대한 예약이 존재하는데, 그 시간을 삭제하려 할 때 400를 반환한다.")
    void return_409_when_delete_id_that_exist_reservation() {
        final long timeId = reservationTimeFixture.예약_시간_생성().id();
        final long themeId = themeService.createTheme(ThemeFixture.getInput())
                                   .id();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput()).id();
        reservationService.createReservation(new ReservationInput("2025-04-30", timeId, themeId,memberId));

        RestAssured.given()
                   .when().delete("/times/" + timeId)
                   .then().statusCode(400);
    }

    @Test
    @DisplayName("중복된 예약 시간을 생성하려 할 때 409를 반환한다.")
    void return_409_when_duplicate_reservationTime() {
        reservationTimeFixture.예약_시간_생성();
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().contentType(ContentType.JSON).body(params)
                   .when().post("/times")
                   .then().statusCode(409);
    }
}
