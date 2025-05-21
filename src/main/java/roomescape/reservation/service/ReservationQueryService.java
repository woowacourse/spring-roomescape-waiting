package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.waiting.service.ReservationWaitingQueryService;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationQueryService {

    private final ReservationQueryManager reservationQueryManager;
    private final ReservationWaitingQueryService waitingQueryService;

    public List<ReservationResponse> getFilteredReservations(Long themeId, Long memberId, LocalDate from,
                                                             LocalDate to) {
        return reservationQueryManager.getFilteredReservations(themeId, memberId, from, to);
    }

    public List<MyReservationResponse> getReservations(Long memberId) {
        List<MyReservationResponse> responses = new ArrayList<>();

        responses.addAll(reservationQueryManager.getReservations(memberId));
        responses.addAll(waitingQueryService.getWaitingReservations(memberId));

        return responses;
    }
}
