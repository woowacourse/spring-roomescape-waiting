package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.User;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationSearchFilter;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.TimeSlotRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
        final ReservationRepository reservationRepository,
        final TimeSlotRepository timeSlotRepository,
        final ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    public Reservation reserve(final User user, final LocalDate date, final long timeId, final long themeId) {
        var timeSlot = getTimeSlotById(timeId);
        var theme = getThemeById(themeId);
        validateDuplicateReservation(date, timeSlot, theme);

        var reservation = Reservation.reserveNewly(user, date, timeSlot, theme);
        var id = reservationRepository.save(reservation);
        return reservation.withId(id);
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findAllReservations(ReservationSearchFilter filter) {
        return reservationRepository.findBySearchFilter(filter);
    }

    public boolean removeById(final long id) {
        return reservationRepository.removeById(id);
    }

    private TimeSlot getTimeSlotById(final long timeId) {
        return timeSlotRepository.findById(timeId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 타임 슬롯입니다."));
    }

    private Theme getThemeById(final long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));
    }

    private void validateDuplicateReservation(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        var reservation = reservationRepository.findByDateAndTimeSlotAndThemeId(date, timeSlot.id(), theme.id());
        if (reservation.isPresent()) {
            throw new IllegalArgumentException("이미 예약된 날짜, 시간, 테마에 대한 예약은 불가능합니다.");
        }
    }
}
