package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.response.ReservationResponse;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyReservationQueryService {

    private final ReservationQueryService reservationQueryService;
    private final WaitingQueryService waitingQueryService;

    public List<ReservationResponse> getMyReservations(String name) {
        return Stream.concat(
                reservationQueryService.getByName(name).stream().map(ReservationResponse::from),
                waitingQueryService.getByName(name).stream().map(ReservationResponse::from)
        )
        .sorted(Comparator.comparing(ReservationResponse::date)
                .thenComparing(response -> response.time().startAt())
                .thenComparing(ReservationResponse::status))
        .toList();
    }
}
