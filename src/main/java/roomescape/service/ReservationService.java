package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.EntityNotFoundException;
import roomescape.query.ReservationQueryRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationCondition;
import roomescape.service.command.ReservationChangeCommand;
import roomescape.service.command.ReservationCommand;
import roomescape.service.command.ReservationSearchCommand;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationSearchResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final Clock clock;

    @Transactional
    public ReservationResult reserve(ReservationCommand command) {
        Reservation reservation = findOrCreateSlotForUpdate(command);
        reservation.reserve(command.name(), command.amount(), LocalDateTime.now(clock));
        return ReservationResult.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResult addWaiting(ReservationCommand command) {
        Reservation reservation = findOrCreateSlotForUpdate(command);
        ReservationEntry added = reservation.reserveOrWait(command.name(), command.amount(), LocalDateTime.now(clock));
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResult.from(saved, saved.findEntryByNameAndStatus(command.name(), added.getStatus()));
    }

    @Transactional
    public ReservationResult change(long entryId, ReservationChangeCommand command) {
        Reservation current = reservationRepository.getByEntryIdForUpdate(entryId);
        ReservationEntry entry = current.findActiveEntry(entryId);

        ReservationTime newTime = findTimeWithThrow(command.timeId());
        if (current.isSameSchedule(command.date(), newTime)) {
            return ReservationResult.from(current, entry);
        }

        return moveEntry(entry, current, command.date(), newTime, LocalDateTime.now(clock));
    }

    private ReservationResult moveEntry(
            ReservationEntry entry,
            Reservation current,
            LocalDate date,
            ReservationTime newTime,
            LocalDateTime now
    ) {
        Reservation target = findOrCreateSlotForUpdate(date, current.getTheme(), newTime);
        ReservationEntry moved = target.reserveOrWait(entry.getReserverName(), now);

        current.cancelEntry(entry.getId());
        reservationRepository.save(current);

        Reservation saved = reservationRepository.save(target);
        return ReservationResult.from(saved,
                saved.findEntryByNameAndStatus(entry.getReserverName(), moved.getStatus()));
    }

    @Transactional
    public void cancelReservation(long entryId) {
        Reservation reservation = reservationRepository.getByEntryIdForUpdate(entryId);

        reservation.cancelEntry(entryId);
        reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationResult confirmPendingEntry(long entryId) {
        Reservation reservation = reservationRepository.getByEntryIdForUpdate(entryId);
        reservation.confirmPendingEntry(entryId);
        reservationRepository.update(reservation);
        return ReservationResult.from(reservation, reservation.findActiveEntry(entryId));
    }

    public ReservationResult getActiveReservationEntry(long entryId) {
        Reservation reservation = reservationRepository.getByEntryId(entryId);
        ReservationEntry reservationEntry = reservation.findActiveEntry(entryId);
        return ReservationResult.from(reservation, reservationEntry);
    }

    public Page<ReservationSearchResult> search(ReservationSearchCommand command, Pageable pageable) {
        return reservationQueryRepository.search(command, pageable);
    }

    public List<ReservationResult> getAllReservations() {
        return reservationQueryRepository.getAllReservations();
    }

    private Reservation findOrCreateSlotForUpdate(LocalDate date, Theme theme, ReservationTime time) {
        ReservationCondition condition = new ReservationCondition(date, theme.getId(), time.getId());
        return reservationRepository.findByDateAndThemeAndTimeForUpdate(condition)
                .orElseGet(() -> Reservation.createSlot(date, theme, time));
    }

    private Reservation findOrCreateSlotForUpdate(ReservationCommand command) {
        return reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition())
                .orElseGet(() -> {
                    Theme theme = findThemeWithThrow(command.themeId());
                    ReservationTime time = findTimeWithThrow(command.timeId());
                    return Reservation.createSlot(command.date(), theme, time);
                });
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
}
