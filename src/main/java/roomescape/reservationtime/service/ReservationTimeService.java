package roomescape.reservationtime.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.exception.model.RoomEscapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.exception.ReservationTimeExceptionCode;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse addReservationTime(ReservationTimeRequest reservationTimeRequest) {
        validateDuplicateTime(reservationTimeRequest.startAt());
        ReservationTime reservationTime = new ReservationTime(reservationTimeRequest.startAt());
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return toResponse(savedReservationTime);
    }

    public List<ReservationTimeResponse> findReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllByOrderByStartAt();

        return reservationTimes.stream()
                .map(this::toResponse)
                .toList();
    }

    public void removeReservationTime(long reservationTimeId) {
        validateReservationExistence(reservationTimeId);
        reservationTimeRepository.deleteById(reservationTimeId);
    }

    public ReservationTimeResponse toResponse(ReservationTime time) {
        return new ReservationTimeResponse(time.getId(), time.getStartAt());
    }

    public void validateDuplicateTime(LocalTime startAt) {
        Optional<ReservationTime> time = reservationTimeRepository.findByStartAt(startAt);

        if (time.isPresent()) {
            throw new RoomEscapeException(ReservationTimeExceptionCode.DUPLICATE_TIME_EXCEPTION);
        }
    }

    public void validateReservationExistence(long timeId) {
        List<Reservation> reservation = reservationRepository.findByTimeId(timeId);

        if (!reservation.isEmpty()) {
            throw new RoomEscapeException(ReservationTimeExceptionCode.EXIST_RESERVATION_AT_CHOOSE_TIME);
        }
    }
}
