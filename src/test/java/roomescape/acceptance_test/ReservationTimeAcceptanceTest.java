package roomescape.acceptance_test;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;

import java.time.LocalTime;

import static roomescape.acceptance_test.step.ReservationTimeAcceptanceSteps.*;

public class ReservationTimeAcceptanceTest extends AcceptanceTestSupport {

    @Test
    @DisplayName("예약 시간 목록 조회")
    public void scenario1() {
        // given
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 30));
        Integer reservationTimeId = 예약_시간_생성을_요청하고(request);

        // when
        ExtractableResponse<Response> response = 예약_시간_목록_조회를_요청하면();

        // then
        생성한_예약_시간이_포함된_예약_시간_목록을_응답받는다(response, reservationTimeId, request);
    }

    @Test
    @DisplayName("중복 예약 시간 생성")
    public void scenario2() {
        // given
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 30));
        예약_시간_생성을_요청하고(request);

        // when
        ExtractableResponse<Response> response = 같은_예약_시간_생성을_다시_요청하면(request);

        // then
        중복_예약_시간_생성은_실패한다(response);
    }

}
