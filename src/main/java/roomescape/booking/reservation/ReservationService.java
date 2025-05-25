package roomescape.booking.reservation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.booking.reservation.dto.AdminFilterReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.reservationtime.ReservationTime;
import roomescape.schedule.Schedule;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public void create(final Reservation reservation) {
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Reservation getById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllByMemberAndThemeAndDateRange(final AdminFilterReservationRequest request) {
        return reservationRepository.findAllByMember_IdAndSchedule_Theme_IdAndSchedule_DateBetween(
                        request.memberId(), request.themeId(),
                        request.from(), request.to()
                ).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllByThemeAndDate(final Theme theme, final LocalDate date) {
        return reservationRepository.findAllBySchedule_ThemeAndSchedule_Date(theme, date);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllByEmail(final String email) {
        return reservationRepository.findAllByMember_Email(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByTheme(final Theme theme) {
        return reservationRepository.existsBySchedule_Theme(theme);
    }

    @Transactional(readOnly = true)
    public boolean existsByReservationTime(final ReservationTime reservationTime) {
        return reservationRepository.existsBySchedule_ReservationTime(reservationTime);
    }

    @Transactional(readOnly = true)
    public boolean existsBySchedule(final Schedule schedule) {
        return reservationRepository.existsBySchedule(schedule);
    }

    @Transactional
    public void deleteById(final Long id) {
        reservationRepository.deleteById(id);
    }
}
