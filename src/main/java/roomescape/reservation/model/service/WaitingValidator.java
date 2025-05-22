package roomescape.reservation.model.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.WaitingRepository;

@Component
@RequiredArgsConstructor
public class WaitingValidator {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public void validateNoDuplication(LocalDate date, Long timeId, Long themeId, Long memberId) {
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId,
            memberId) ||
            reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId,
                memberId)) {
            throw new InvalidReservationTimeException("이미 대기중인 예약입니다.");
        }
    }
}
