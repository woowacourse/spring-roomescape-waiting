package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.BaseControllerTest;

class MemberPageControllerTest extends BaseControllerTest {

    @DisplayName("멤버 예약 페이지로 이동한다.")
    @Test
    void responseReservationPage() {
        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .when().get("/reservation")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("홈 화면은 로그인하지 않아도 접속할 수 있다.")
    @Test
    void responseMainPage() {
        RestAssured.given().log().all()
                .when().get("")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("현재 로그인된 멤버의 예약 내역 조회 페이지로 이동한다.")
    @Test
    void responseUserReservation() {
        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .when().get("/reservation-mine")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }
}
