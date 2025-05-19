package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.byDate;
import static roomescape.infrastructure.ReservationSpecs.byFilter;
import static roomescape.infrastructure.ReservationSpecs.byThemeId;
import static roomescape.infrastructure.ReservationSpecs.byTimeSlotId;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public Reservation reserve(final long userId, final LocalDate date, final long timeId, final long themeId) {
        var timeSlot = timeSlotRepository.getById(timeId);
        var theme = themeRepository.getById(themeId);
        validateDuplicateReservation(date, timeSlot, theme);
        var user = userRepository.getById(userId);

        return reservationRepository.save(new Reservation(user, date, timeSlot, theme));
    }

    public List<Reservation> findAllReservations(ReservationSearchFilter filter) {
        return reservationRepository.findAll(byFilter(filter));
    }

    public void removeById(final long id) {
        var reservation = reservationRepository.getById(id);
        reservationRepository.delete(reservation);
    }

    private void validateDuplicateReservation(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        var byDateTimeAndThemeId = Specification.allOf(byDate(date), byTimeSlotId(timeSlot.id()), byThemeId(theme.id()));
        if (reservationRepository.exists(byDateTimeAndThemeId)) {
            throw new AlreadyExistedException("이미 예약된 날짜, 시간, 테마에 대한 예약은 불가능합니다.");
        }
    }
}
