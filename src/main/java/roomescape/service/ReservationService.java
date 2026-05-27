package roomescape.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.EntityNotFoundException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationCondition;
import roomescape.service.command.ReservationChangeCommand;
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
        Reservation reservation = reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition())
                .orElseGet(() -> {
                    Theme theme = findThemeWithThrow(command.themeId());
                    ReservationTime time = findTimeWithThrow(command.timeId());
                    return Reservation.createSlot(command.date(), theme, time);
                });
        reservation.reserve(command.name());
        return ReservationResult.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResult change(long entryId, ReservationChangeCommand command) {
        Reservation currentReservation = reservationRepository.findByEntryIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
        ReservationEntry entry = currentReservation.findReservedEntry(entryId);

        ReservationTime time = findTimeWithThrow(command.timeId());
        if (currentReservation.isSameSlot(command.date(), time)) {
            return ReservationResult.from(currentReservation, entryId);
        }

        Theme theme = currentReservation.getTheme();
        ReservationCondition condition = command.toCondition(theme.getId());
        Reservation newReservation = reservationRepository.findByDateAndThemeAndTimeForUpdate(condition)
                .orElseGet(() -> Reservation.createSlot(command.date(), theme, time));
        newReservation.reserve(entry.getName());

        currentReservation.cancelEntry(entryId);
        reservationRepository.save(currentReservation);

        return ReservationResult.from(reservationRepository.save(newReservation));
    }

    @Transactional
    public void cancelReservation(long entryId) {
        Reservation reservation = reservationRepository.findByEntryIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));

        reservation.cancelEntry(entryId);
        reservationRepository.save(reservation);
    }

    public List<ReservationResult> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResult::from)
                .toList();
    }

    private Reservation findReservationByEntryIdWithThrow(long entryId) {
        return reservationRepository.findByEntryIdForUpdate(entryId)
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

    public ReservationResult getReservationEntry(long entryId) {
        return ReservationResult.from(findReservationByEntryIdWithThrow(entryId), entryId);
    }
}
