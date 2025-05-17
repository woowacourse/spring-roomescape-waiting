package roomescape.time.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.domain.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeQueryServiceImpl implements ReservationTimeQueryService {

    private final ReservationTimeRepository reservationTimeRepository;

    @Override
    public ReservationTime get(final ReservationTimeId id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<ReservationTime> getAll() {
        return reservationTimeRepository.findAll();
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeRepository.existsByStartAt(startAt);
    }

    @Override
    public boolean existById(final ReservationTimeId id) {
        return reservationTimeRepository.existsById(id);
    }
}
