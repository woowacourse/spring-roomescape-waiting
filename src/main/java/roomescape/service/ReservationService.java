package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.DuplicateEntityException;
import roomescape.domain.EntityNotFoundException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.command.ReservationCommand;
import roomescape.service.result.ReservationResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public ReservationResult reserve(ReservationCommand command) {
        Theme theme = findThemeWithThrow(command.themeId());
        ReservationTime time = findTimeWithThrow(command.timeId());
        validateAlreadyReservation(command.date(), theme, time);

        Reservation reservation = Reservation.createNew(command.name(), command.date(), theme, time);
        Reservation saved = reservationRepository.save(reservation);

        return ReservationResult.from(saved);
    }

    @Transactional
    public ReservationResult change(long id, ReservationCommand command) {
        Reservation reservation = findReservationWithThrow(id);
        ReservationTime time = findTimeWithThrow(command.timeId());
        if (reservation.isSameTime(command.date(), time)) {
            return ReservationResult.from(reservation);
        }

        validateAlreadyReservation(command.date(), reservation.getTheme(), time);
        reservation.update(command.date(), time);
        reservationRepository.update(reservation);

        return ReservationResult.from(reservation);
    }

    @Transactional
    public void cancelReservation(long id) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> {
                    reservation.validateNotPast();
                    reservationRepository.delete(id);
                });
    }

    public List<ReservationResult> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResult::from)
                .toList();
    }

    private void validateAlreadyReservation(LocalDate date, Theme theme, ReservationTime time) {
        if (reservationRepository.existByDateAndThemeIdAndTimeId(date, theme.getId(), time.getId())) {
            throw new DuplicateEntityException("이미 예약 된 날짜입니다. (%s %s)", date, time.getStartAt());
        }
    }

    private Reservation findReservationWithThrow(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
    }

    private Theme findThemeWithThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .filter(Theme::isActive)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마 정보입니다."));
    }

    private ReservationTime findTimeWithThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .filter(ReservationTime::isActive)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간 정보입니다."));
    }

    public ReservationResult getReservation(long id) {
        return ReservationResult.from(findReservationWithThrow(id));
    }
}
