package roomescape.controller.dto.response;

import roomescape.service.dto.result.ReservationTimeDetailResult;

public record ReservationTimeDetailResponse (
        ReservationTimeResponse timeResponse,
        boolean isReserved
){
    public static ReservationTimeDetailResponse from(ReservationTimeDetailResult result){
        return new ReservationTimeDetailResponse(
                ReservationTimeResponse.from(result.timeResult()),
                result.isReserved()
        );
    }
}
