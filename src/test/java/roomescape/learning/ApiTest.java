package roomescape.learning;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.auth.stub.StubTokenProvider;
import roomescape.common.CleanUp;
import roomescape.config.AuthServiceTestConfig;

@Import(AuthServiceTestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CleanUp cleanUp;

    private Map<String, String> theme;
    private Map<String, String> reservationTime;
    private Map<String, Object> reservation;
    private Map<String, String> member;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        member = Map.of("email", "user1@email.com", "password", "1234", "name", "브라운");
        theme = Map.of("name", "테마1", "description", "설명1", "thumbnail", "썸네일1");
        reservationTime = Map.of("startAt", "10:00");
        reservation = new HashMap<>();
        reservation.put("memberId", 1L);
        reservation.put("date", "2025-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        cleanUp.all();
    }

    @Test
    void 방탈출_예약을_생성_조회_삭제한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(member)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", StubTokenProvider.USER_STUB_TOKEN)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("data.size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("data.size()", is(0));
    }

    @Test
    void 예약_시간을_생성_조회_삭제한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("data.size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 관리자_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", StubTokenProvider.ADMIN_STUB_TOKEN)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 방탈출_예약_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", StubTokenProvider.ADMIN_STUB_TOKEN)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 방탈출_예약_목록을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("data.size()", is(0));
    }

    @Test
    void 방탈출_예약_생성시_예약자_이름이_비어있으면_예외를_응답한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        reservation.remove("name");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", StubTokenProvider.USER_STUB_TOKEN)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약_삭제시_존재하지_않는_예약이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약_시간_삭제시_존재하지_않는_예약시간이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약_생성_시_null_값을_허용하지_않는다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(member)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        reservation.remove("date");
        reservation.put("date", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", StubTokenProvider.USER_STUB_TOKEN)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
