package roomescape.validation;

import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.domain.TimeSlot;
import roomescape.repository.ReservationRepository;
import roomescape.repository.TimeSlotRepository;

@Component
public class TimeSlotValidator {

    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;

    public TimeSlotValidator(TimeSlotRepository timeSlotRepository, ReservationRepository reservationRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
    }

    public void validateDuplicatedTime(LocalTime startAt) {
        if (timeSlotRepository.existsByStartAt(startAt)) {
            throw new IllegalArgumentException("이미 등록된 시간입니다");
        }
    }

    public void validateExistReservation(TimeSlot timeSlot) {
        if (reservationRepository.existsByTime(timeSlot)) {
            throw new IllegalArgumentException("예약이 등록된 시간은 제거할 수 없습니다");
        }
    }
}
