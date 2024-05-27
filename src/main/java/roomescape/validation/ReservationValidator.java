package roomescape.validation;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.repository.ReservationRepository;


@Component
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public ReservationValidator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateDuplicatedReservation(Member member, LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (reservationRepository.existsByMemberAndDateAndTimeAndTheme(member, date, timeSlot, theme)) {
            throw new IllegalArgumentException("이미 예약을 시도 하였습니다.");
        }
    }
}
