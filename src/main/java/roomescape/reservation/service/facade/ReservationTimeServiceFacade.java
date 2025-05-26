package roomescape.reservation.service.facade;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.service.time.ReservationTimeService;

@Service
@RequiredArgsConstructor
public class ReservationTimeServiceFacade {

    private final ReservationTimeService reservationTimeService;

    @Transactional
    public ReservationTimeResponse save(final ReservationTimeRequest request) {
        final LocalTime startAt = request.startAt();

        final ReservationTime savedReservationTime = reservationTimeService.save(startAt);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    @Transactional
    public void deleteById(final Long id) {
        reservationTimeService.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        final List<ReservationTime> reservationTimes = reservationTimeService.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }
}

