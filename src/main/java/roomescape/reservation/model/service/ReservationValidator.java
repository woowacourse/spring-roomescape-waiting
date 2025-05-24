package roomescape.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.exception.ReservationException.ReservationNotFoundException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.vo.Schedule;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public void validateNoDuplication(Schedule schedule) {
        if (reservationRepository.existDuplicatedSchedule(schedule)) {
            throw new InvalidReservationTimeException("이미 예약된 시간입니다. 다른 시간을 예약해주세요.");
        }
    }

    public void validateExistenceBySchedule(Schedule schedule) {
        if (!reservationRepository.existDuplicatedSchedule(schedule)) {
            throw new ReservationNotFoundException("예약이 없는 상태에서 예약 대기를 생성할 수 없습니다. 예약을 신청해주세요.");
        }
    }
}
