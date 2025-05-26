package roomescape.domain.reservation;

import org.springframework.stereotype.Component;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.exception.DuplicateContentException;
import roomescape.repository.JpaReservationRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReservationCreator {

    private final JpaReservationRepository reservationRepository;

    public ReservationCreator(JpaReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation create(Member member, LocalDate date, ReservationTime time, Theme theme) {
        validateDuplicate(date, time.getId(), theme.getId());
        Reservation.validateReservableTime(date, time.getStartAt());

        return Reservation.createWithoutId(member, date, time, theme);
    }

    private void validateDuplicate(LocalDate date, long timeId, long themeId) {
        List<Reservation> reservations = reservationRepository.findReservationsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (!reservations.isEmpty()) {
            throw new DuplicateContentException("[ERROR] 이미 예약이 존재합니다. 다른 예약 일정을 선택해주세요.");
        }
    }
}
