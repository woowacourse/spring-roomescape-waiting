package roomescape.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.EntityNotFoundException;
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
        Reservation current = reservationRepository.findByEntryIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
        ReservationEntry entry = current.findReservedEntry(entryId);

        ReservationTime newTime = findTimeWithThrow(command.timeId());
        if (current.isSameSlot(command.date(), newTime)) {
            return ReservationResult.from(current, entry);
        }

        return moveEntry(entry, current, command.date(), newTime);
    }

    private ReservationResult moveEntry(ReservationEntry entry, Reservation current,
                                        LocalDate date, ReservationTime newTime) {
        Reservation target = findOrCreateSlot(date, current.getTheme(), newTime);
        ReservationEntry moved = target.reserve(entry.getName());

        current.cancelEntry(entry.getId());
        reservationRepository.save(current);

        Reservation saved = reservationRepository.save(target);
        return ReservationResult.from(saved, saved.findEntryByNameAndStatus(entry.getName(), moved.getStatus()));
    }

    private Reservation findOrCreateSlot(LocalDate date, Theme theme, ReservationTime time) {
        ReservationCondition condition = new ReservationCondition(date, theme.getId(), time.getId());
        return reservationRepository.findByDateAndThemeAndTimeForUpdate(condition)
                .orElseGet(() -> Reservation.createSlot(date, theme, time));
    }

    @Transactional
    public ReservationResult addWaiting(ReservationCommand command) {
        Reservation reservation = reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition())
                .orElseGet(() -> {
                    Theme theme = findThemeWithThrow(command.themeId());
                    ReservationTime time = findTimeWithThrow(command.timeId());
                    return Reservation.createSlot(command.date(), theme, time);
                });
        ReservationEntry added = reservation.joinWaitingList(command.name());
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResult.from(saved, saved.findEntryByNameAndStatus(command.name(), added.getStatus()));
    }

    @Transactional
    public void cancelReservation(long entryId) {
        Reservation reservation = reservationRepository.findByEntryIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));

        reservation.cancelEntry(entryId);
        reservationRepository.save(reservation);
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
        Reservation reservation = findReservationByEntryIdWithThrow(entryId);
        ReservationEntry reservationEntry = reservation.findReservedEntry(entryId);
        return ReservationResult.from(reservation, reservationEntry);
    }

    private Reservation findReservationByEntryIdWithThrow(long entryId) {
        return reservationRepository.findByEntryIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
    }
}
