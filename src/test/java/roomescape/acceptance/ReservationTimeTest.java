package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.service.dto.request.MemberRequest;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationTimeTest extends AcceptanceTest{

    private String accessToken;

    @BeforeEach
    void insert() {
        TokenRequest tokenRequest = new TokenRequest("password", "admin@email.com");
        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationTimeRequest)
                .post("/times");

        ThemeRequest themeRequest = new ThemeRequest("hi", "happy", "abcd.html");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(themeRequest)
                .post("/themes");

        MemberRequest member = new MemberRequest("sudal", "aa@aa", "aa", Role.ADMIN);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(member)
                .post("/members");


        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), 1L, 1L, 2L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationRequest)
                .post("/reservations");
    }

    @DisplayName("올바르지 않은 시간 형식으로 입력시 예외처리")
    @Test
    void invalidTimeFormat() {
        Map<String, String> reservationTimeRequest = new HashMap<>();
        reservationTimeRequest.put("startAt", "aaa11");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약되어 있는 시간 삭제시 예외처리")
    @Test
    void deleteTimeInUse() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("테마와 날짜 정보를 주면 시간별 예약가능 여부를 반환한다.")
    @Test
    void availableTime() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/times/availability?themeId=1&date=2999-12-12")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }
}
