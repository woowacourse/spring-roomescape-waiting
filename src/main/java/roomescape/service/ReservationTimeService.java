package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeJpaRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationTimeJpaRepository reservationTimeJpaRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTime create(ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = ReservationTime.of(request.getStartAt());
        return reservationTimeJpaRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll();
    }

    public List<ReservationTime> findAvailable(AvailableTimeFindRequest request, LocalDate now) {
        if (now.isAfter(request.getDate())) {
            throw new RoomEscapeException(ErrorCode.PAST_DATE_NOT_ALLOWED);
        }

        return reservationTimeRepository.findByDateAndTheme(request.getDate(), request.getThemeId());
    }

    @Transactional
    public void delete(long reservationTimeId) {
        if (!reservationTimeJpaRepository.existsById(reservationTimeId)) {
            throw new RoomEscapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND);
        }

        if (reservationRepository.existsByTimeId(reservationTimeId)) {
            throw new RoomEscapeException(ErrorCode.RESERVATION_TIME_IN_USE);
        }

        reservationTimeJpaRepository.deleteById(reservationTimeId);
    }
}
