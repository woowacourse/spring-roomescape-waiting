package roomescape.reservationtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.common.exception.DomainException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    private final Clock clock;

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        ReservationTime reservationTime = ReservationTime.create(startAt);
        validateNotDuplicated(reservationTime);

        return reservationTimeRepository.save(reservationTime);
    }

    private void validateNotDuplicated(ReservationTime reservationTime) {
        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new DomainException(RESERVATION_TIME_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existByTimeId(id)) {
            throw new DomainException(RESERVATION_TIME_HAS_RESERVATION);
        }

        if (!reservationTimeRepository.cancelById(id, LocalDateTime.now(clock))) {
            throw new DomainException(RESERVATION_TIME_NOT_FOUND);
        }
    }
}
