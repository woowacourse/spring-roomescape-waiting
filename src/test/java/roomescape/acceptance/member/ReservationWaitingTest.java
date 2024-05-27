package roomescape.acceptance.member;

import static org.hamcrest.Matchers.containsString;

import java.time.LocalDate;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.Fixture;
import roomescape.dto.request.ReservationWaitingRequest;
import roomescape.dto.response.MemberReservationResponse;
import roomescape.dto.response.ReservationWaitingResponse;
import roomescape.util.JwtProvider;

public class ReservationWaitingTest extends BaseAcceptanceTest {

    @Autowired
    JwtProvider jwtProvider;

    @DisplayName("예약 대기를 추가, 조회, 취소한다.")
    @Test
    void test() {
        ReservationWaitingRequest request = new ReservationWaitingRequest(LocalDate.parse("2024-05-01"), 2L, 2L);

        // 추가
        ReservationWaitingResponse createdWaiting = RestAssured.given().log().all()
                .cookie("token", Fixture.customerToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waitings")
                .then().log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", containsString("/reservations/waitings/"))
                .extract().as(ReservationWaitingResponse.class);

        // 조회
        TypeRef<List<MemberReservationResponse>> reservationResponse = new TypeRef<>() {};
        List<MemberReservationResponse> responses = RestAssured.given().log().all()
                .cookie("token", Fixture.customerToken)
                .contentType(ContentType.JSON)
                .when().get("/reservations/my")
                .then().log().all()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(reservationResponse);

        // 취소
        RestAssured.given().log().all()
                .cookie("token", Fixture.customerToken)
                .when().delete("/reservations/waitings/" + createdWaiting.id())
                .then().log().all()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @DisplayName("선택한 날짜, 테마, 시간에 예약이 존재하는지 확인한다.")
    @Nested
    class CheckReservationExists {

//        @Test
//        void checkExists() {
//            Reservation savedReservation = PRE_INSERTED_RESERVATION_1;
//            String requestParams = selectedReservationParameters(
//                    savedReservation.getDate().toString(),
//                    savedReservation.getReservationTime().getId(),
//                    savedReservation.getTheme().getId()
//            );
//
//            RestAssured.given().log().all()
//                    .cookie("token", Fixture.customerToken)
//                    .contentType(ContentType.JSON)
//                    .when().get("/reservations/" + requestParams)
//                    .then().log().all()
//                    .statusCode(HttpStatus.SC_OK);
//        }

        @Test
        void checkNotExists() {
            String requestParams = selectedReservationParameters("2024-05-01", 1L, 1L);

            RestAssured.given().log().all()
                    .cookie("token", Fixture.customerToken)
                    .contentType(ContentType.JSON)
                    .when().get("/reservations/" + requestParams)
                    .then().log().all()
                    .statusCode(HttpStatus.SC_NOT_FOUND);
        }

        String selectedReservationParameters(String date, Long timeId, Long themeId) {
            return "?date=" + date + "&timeId=" + timeId + "&themeId=" + themeId;
        }
    }

}
