package roomescape.reservationtime.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.exception.ReservationTimeErrorCode;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTimeResponse addReservationTime(
            ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = ReservationTime.create(reservationTimeRequest.startAt());

        validateDuplicateReservationTime(reservationTime);
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedTime);
    }

    private void validateDuplicateReservationTime(ReservationTime reservationTime) {
        if (reservationTimeRepository.existByStartAt(reservationTime.getStartAt())) {
            throw new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_DUPLICATE);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAllReservationTime() {
        return reservationTimeRepository.findAll().stream().map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        validateReservationTimeExists(id);
        validateRemovableReservationTime(id);
        reservationTimeRepository.delete(id);
    }

    private void validateReservationTimeExists(Long id) {
        reservationTimeRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND)
        );
    }

    private void validateRemovableReservationTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_EXIST_ON_TIME);
        }
    }
}
