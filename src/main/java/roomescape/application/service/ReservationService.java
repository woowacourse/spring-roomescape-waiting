package roomescape.application.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.command.ReservationChangeCommand;
import roomescape.application.service.command.ReservationCommand;
import roomescape.application.service.command.ReservationPendingCommand;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.EntityNotFoundException;
import roomescape.persistence.ReservationSlotRepository;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.persistence.ThemeRepository;
import roomescape.persistence.dto.ReservationCondition;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationSlotRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public ReservationSlotResult createPending(ReservationPendingCommand command) {
        ReservationSlot slot = findOrCreateSlot(command.date(), command.themeId(), command.timeId());
        Reservation reservation = slot.pending(command.name(), command.status());
        ReservationSlot saved = reservationRepository.save(slot);
        return ReservationSlotResult.from(saved, saved.findLatestPendingEntryByName(reservation.getName()));
    }

    @Transactional
    public ReservationSlotResult reserve(ReservationCommand command) {
        ReservationSlot slot = findOrCreateSlot(command.date(), command.themeId(), command.timeId());
        Reservation reservation = slot.reserve(command.name());
        return saveAndCreateResult(slot, reservation);
    }

    @Transactional
    public ReservationSlotResult change(long reservationId, ReservationChangeCommand command) {
        ReservationSlot current = findReservationSlot(reservationId);
        Reservation reservation = current.findReservedReservation(reservationId);

        ReservationTime newTime = findTimeWithThrow(command.timeId());
        if (current.isSameSlot(command.date(), newTime)) {
            return ReservationSlotResult.from(current, reservation);
        }

        ReservationSlot target = findOrCreateSlot(command.date(), current.getTheme(), newTime);
        return moveReservation(reservation, current, target);
    }

    @Transactional
    public ReservationSlotResult addWaiting(ReservationCommand command) {
        ReservationSlot slot = findOrCreateSlot(command.date(), command.themeId(), command.timeId());
        Reservation added = slot.joinWaitingList(command.name());
        return saveAndCreateResult(slot, added);
    }

    @Transactional
    public void cancelReservation(long reservationId) {
        ReservationSlot slot = findReservationSlot(reservationId);
        slot.cancelReservation(reservationId);
        reservationRepository.save(slot);
    }

    @Transactional
    public void confirmPending(long reservationId, ReservationStatus status) {
        ReservationSlot slot = findReservationSlot(reservationId);
        slot.confirmPendingReservation(reservationId, status);
        reservationRepository.save(slot);
    }

    private ReservationSlotResult moveReservation(
            Reservation reservation,
            ReservationSlot current,
            ReservationSlot target
    ) {
        Reservation moved = target.reserve(reservation.getName());

        current.cancelReservation(reservation.getId());
        reservationRepository.save(current);

        return saveAndCreateResult(target, moved);
    }

    private ReservationSlotResult saveAndCreateResult(ReservationSlot slot, Reservation reservation) {
        ReservationSlot saved = reservationRepository.save(slot);
        return ReservationSlotResult.from(saved, saved.findActiveEntryByName(reservation.getName()));
    }

    private ReservationSlot findOrCreateSlot(LocalDate date, long themeId, long timeId) {
        Theme theme = findThemeWithThrow(themeId);
        ReservationTime time = findTimeWithThrow(timeId);
        return findOrCreateSlot(date, theme, time);
    }

    private ReservationSlot findOrCreateSlot(LocalDate date, Theme theme, ReservationTime time) {
        ReservationCondition condition = new ReservationCondition(date, theme.getId(), time.getId());
        return reservationRepository.findByDateAndThemeAndTimeForUpdate(condition)
                .orElseGet(() -> ReservationSlot.createSlot(date, theme, time));
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

    private ReservationSlot findReservationSlot(long reservationId) {
        return reservationRepository.findByReservationIdForUpdate(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
    }
}
