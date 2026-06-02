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
        Reservation reservation = reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition())
                .orElseGet(() -> {
                    Theme theme = findThemeWithThrow(command.themeId());
                    ReservationTime time = findTimeWithThrow(command.timeId());
                    return Reservation.createSlot(command.date(), theme, time);
                });
        reservation.reserve(command.name(), LocalDateTime.now(clock));
        return ReservationResult.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResult change(long entryId, ReservationChangeCommand command) {
        Reservation current = reservationRepository.findByEntryIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
        ReservationEntry entry = current.findActiveEntry(entryId);

        ReservationTime newTime = findTimeWithThrow(command.timeId());
        if (current.isSameSlot(command.date(), newTime)) {
            return ReservationResult.from(current, entry);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        return moveEntry(entry, current, command.date(), newTime, now);
    }

    private ReservationResult moveEntry(ReservationEntry entry, Reservation current,
                                        LocalDate date, ReservationTime newTime, LocalDateTime now) {
        Reservation target = findOrCreateSlot(date, current.getTheme(), newTime);
        ReservationEntry moved = target.joinWaitingList(entry.getName(), now);

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
        ReservationEntry added = reservation.joinWaitingList(command.name(), LocalDateTime.now(clock));
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

    public ReservationResult getActiveReservationEntry(long entryId) {
        Reservation reservation = findReservationByEntryIdWithThrow(entryId);
        ReservationEntry reservationEntry = reservation.findActiveEntry(entryId);
        return ReservationResult.from(reservation, reservationEntry);
    }

    public List<ReservationResult> getAllReservations() {
        return reservationQueryRepository.getAllReservations();
    }

    public Page<ReservationSearchResult> search(ReservationSearchCommand command, Pageable pageable) {
        return reservationQueryRepository.search(command, pageable);
    }

    private Reservation findReservationByEntryIdWithThrow(long entryId) {
        return reservationRepository.findByEntryId(entryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
    }
}
