package roomescape.dto.response;

import roomescape.domain.Reservation;
import roomescape.dto.projection.MemberSummaryProjection;
import roomescape.dto.result.StoreReservationResult;

import java.util.List;

public record StoreReservationResponse(
        Long id,
        Long memberId,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        StoreResponse store,
        MemberSummaryProjection member
) {
    public static StoreReservationResponse from(StoreReservationResult storeReservationResult) {
        Reservation reservation = storeReservationResult.reservation();
        return new StoreReservationResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getDate().toString(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(storeReservationResult.theme()),
                StoreResponse.from(storeReservationResult.store()),
                storeReservationResult.member()
        );
    }

    public static List<StoreReservationResponse> fromAll(List<StoreReservationResult> storeReservationResults) {
        return storeReservationResults.stream()
                .map(StoreReservationResponse::from)
                .toList();
    }
}
