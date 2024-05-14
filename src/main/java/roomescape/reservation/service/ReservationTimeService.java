package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;
import roomescape.reservation.controller.dto.AvailableTimeResponse;
import roomescape.reservation.controller.dto.ReservationTimeRequest;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.domain.AvailableTimes;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse create(ReservationTimeRequest reservationTimeRequest) {
        LocalTime time = LocalTime.parse(reservationTimeRequest.startAt());
        if (reservationTimeRepository.existsByStartAt(time)) {
            throw new BusinessException(ErrorType.DUPLICATED_RESERVATION_TIME_ERROR);
        }

        ReservationTime reservationTime = new ReservationTime(time);
        return ReservationTimeResponse.from(reservationTimeRepository.save(reservationTime));
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public void delete(long timeId) {
        if (reservationRepository.existsByTimeId(timeId)) {
            throw new BusinessException(ErrorType.RESERVATION_NOT_DELETED);
        }
        reservationTimeRepository.deleteById(timeId);
    }

    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, long themeId) {
        List<ReservationTime> times = reservationTimeRepository.findAll();
        Set<ReservationTime> reservedTimes = reservationTimeRepository.findReservedTime(date, themeId);

        return AvailableTimes.of(times, reservedTimes).getAvailableTimes()
                .stream()
                .map(AvailableTimeResponse::from)
                .toList();
    }
}
