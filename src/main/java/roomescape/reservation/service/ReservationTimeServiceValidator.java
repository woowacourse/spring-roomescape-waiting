package roomescape.reservation.service;

import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

@Component
public class ReservationTimeServiceValidator {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeServiceValidator(ReservationTimeRepository reservationTimeRepository,
                                           ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void checkAlreadyExistsTime(LocalTime time) {
        if (reservationTimeRepository.existsByStartAt(time)) {
            throw new IllegalArgumentException("생성하려는 시간 " + time + "가 이미 존재합니다. 시간을 생성할 수 없습니다.");
        }
    }

    public void validateExistReservationTime(Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 시간이 존재하지 않습니다. 삭제가 불가능합니다.");
        }
    }

    public void validateReservationTimeUsage(Long id) {
        if (reservationRepository.existsBySlot_ReservationTimeId(id)) {
            throw new IllegalStateException("식별자 " + id + "인 시간을 사용 중인 예약이 존재합니다. 삭제가 불가능합니다.");
        }
    }
}
