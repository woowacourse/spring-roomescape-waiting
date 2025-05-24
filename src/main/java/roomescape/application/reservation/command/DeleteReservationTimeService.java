package roomescape.application.reservation.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.infrastructure.error.exception.ReservationTimeException;

@Service
@Transactional
public class DeleteReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public DeleteReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                        ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void removeById(Long reservationTimeId) {
        validateReservationTimeIsNotReserved(reservationTimeId);
        reservationTimeRepository.deleteById(reservationTimeId);
    }

    private void validateReservationTimeIsNotReserved(Long reservationTimeId) {
        if (reservationRepository.existsByTimeId(reservationTimeId)) {
            throw new ReservationTimeException("해당 예약 시간으로 예약된 예약이 존재합니다.");
        }
    }
}
