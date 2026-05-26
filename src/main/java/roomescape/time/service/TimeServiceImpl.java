package roomescape.time.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.exception.ReservationTimeConflictException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.TimeRepository;

@Service
public class TimeServiceImpl implements TimeService {
    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public TimeServiceImpl(TimeRepository timeRepository, ReservationRepository reservationRepository) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public ReservationTime create(LocalDateTime startAt, LocalDateTime endAt) {
        return timeRepository.save(startAt, endAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        return timeRepository.findAll();
    }

    @Override
    public List<ReservationTime> findByDate(LocalDate date) {
        return timeRepository.findByDate(date);
    }

    @Override
    public ReservationTime findById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new TimeNotFoundException(id));
    }

    @Override
    public void deleteById(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new ReservationTimeConflictException(id);
        }
        boolean deleted = timeRepository.deleteById(id);
        if (!deleted) {
            throw new TimeNotFoundException(id);
        }
    }
}
