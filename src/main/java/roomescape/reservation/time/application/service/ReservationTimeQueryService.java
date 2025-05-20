package roomescape.reservation.time.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.domain.ReservationTimeId;
import roomescape.reservation.time.domain.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTime get(final ReservationTimeId id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    public List<ReservationTime> getAll() {
        return reservationTimeRepository.findAll();
    }

    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeRepository.existsByStartAt(startAt);
    }

    public boolean existById(final ReservationTimeId id) {
        return reservationTimeRepository.existsById(id);
    }
}
