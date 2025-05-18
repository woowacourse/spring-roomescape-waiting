package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.byDate;
import static roomescape.infrastructure.ReservationSpecs.byFilter;
import static roomescape.infrastructure.ReservationSpecs.byThemeId;
import static roomescape.infrastructure.ReservationSpecs.byTimeSlotId;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final TimeSlotRepository timeSlotRepository,
            final ThemeRepository themeRepository,
            final UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }

    public Reservation reserve(final long userId, final LocalDate date, final long timeId, final long themeId) {
        var timeSlot = getTimeSlotById(timeId);
        var theme = getThemeById(themeId);
        validateDuplicateReservation(date, timeSlot, theme);

        var user = getUserById(userId);
        var reservation = new Reservation(user, date, timeSlot, theme);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAllReservations(ReservationSearchFilter filter) {
        return reservationRepository.findAll(byFilter(filter));
    }

    public void removeById(final long id) {
        reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
        reservationRepository.deleteById(id);
    }

    private TimeSlot getTimeSlotById(final long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 타임 슬롯입니다."));
    }

    private Theme getThemeById(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }

    private User getUserById(final long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

    private void validateDuplicateReservation(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        var byDateTimeAndThemeId = Specification.allOf(byDate(date), byTimeSlotId(timeSlot.id()), byThemeId(theme.id()));
        var reservation = reservationRepository.findAll(byDateTimeAndThemeId);
        if (!reservation.isEmpty()) {
            throw new AlreadyExistedException("이미 예약된 날짜, 시간, 테마에 대한 예약은 불가능합니다.");
        }
    }
}
