package roomescape.acceptance.member;

import static org.hamcrest.Matchers.containsString;

import static roomescape.util.CookieUtil.TOKEN_NAME;

import java.time.LocalDate;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.Fixture;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.util.JwtProvider;

public class ReservationWaitingTest extends BaseAcceptanceTest {

    @Autowired
    JwtProvider jwtProvider;

    @DisplayName("예약 대기를 추가, 조회, 취소한다.")
    @Test
    void test() {
        ReservationRequest request = new ReservationRequest(null, LocalDate.parse("2024-05-02"), 3L, 3L);

        // 추가
        ReservationWaitingResponse createdWaiting = RestAssured.given().log().all()
                .cookie(TOKEN_NAME, Fixture.customerToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waitings")
                .then().log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", containsString("/reservations/waitings/"))
                .extract().as(ReservationWaitingResponse.class);

        // 조회
        TypeRef<List<MyReservationResponse>> reservationResponse = new TypeRef<>() {};
        RestAssured.given().log().all()
                .cookie(TOKEN_NAME, Fixture.customerToken)
                .contentType(ContentType.JSON)
                .when().get("/reservations/my")
                .then().log().all()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(reservationResponse);

        // 취소
        RestAssured.given().log().all()
                .cookie(TOKEN_NAME, Fixture.customerToken)
                .when().delete("/reservations/waitings/" + createdWaiting.id())
                .then().log().all()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
