package roomescape.reservation.application;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.application.OrderService;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.reservation.application.exception.ReservationNotFoundException;
import roomescape.reservation.domain.TimeSlot;
import roomescape.theme.application.ThemeService;
import roomescape.theme.domain.Theme;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.domain.ReservationTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationManager {

    private final ActiveReservationService activeReservationService;
    private final PendingReservationService pendingReservationService;
    private final ReservationTimeService timeService;
    private final ThemeService themeService;
    private final TimeSlotService timeSlotService;
    private final OrderService orderService;

    public List<ReservationInfo> getReservations() {
        List<ReservationInfo> activeReservations = activeReservationService.getReservations();
        List<ReservationInfo> pendingReservations = pendingReservationService.getReservations();
        return Stream.concat(activeReservations.stream(), pendingReservations.stream())
                .toList();
    }

    public List<ReservationPendingInfo> getReservationsByName(final String username) {
        List<ReservationPendingInfo> activeReservations = activeReservationService.getReservationsByName(username);
        List<ReservationPendingInfo> pendingReservations = pendingReservationService.getReservationsByName(username);
        return Stream.concat(activeReservations.stream(), pendingReservations.stream())
                .toList();
    }

    @Transactional
    public ReservationInfo addReservation(final ReservationCreateCommand command) {
        ReservationTime time = timeService.getTime(command.timeId(), command.date());
        Theme theme = themeService.getThemeById(command.themeId());
        TimeSlot slot = timeSlotService.getTimeSlot(command.date(), time, theme);
        try {
            ReservationInfo reservation = activeReservationService.add(slot, command);
            orderService.createOrder(reservation.id());
            return reservation;
        } catch (ReservationInUseException e) {
            return pendingReservationService.add(slot, command);
        }
    }

    @Transactional
    public void cancelReservation(final Long id, final ReservationCancelCommand command) {
        if (pendingReservationService.existsById(id)) {
            pendingReservationService.cancel(id, command.name());
            return;
        }
        if (activeReservationService.existsById(id)) {
            Long slotId = activeReservationService.cancel(id, command.name());
            promoteNextPending(slotId);
            return;
        }
        throw new ReservationNotFoundException("해당 예약을 찾을 수 없습니다.");
    }

    @Transactional
    public ReservationInfo changeReservation(final Long id, final ReservationChangeCommand command) {
        ReservationTime time = timeService.getTime(command.timeId(), command.date());
        Theme theme = themeService.getThemeById(command.themeId());
        TimeSlot slot = timeSlotService.getTimeSlot(command.date(), time, theme);

        if (pendingReservationService.existsById(id)) {
            boolean isSlotFull = activeReservationService.existsBySlotId(slot.getId());
            return changePendingReservation(id, command, slot, isSlotFull);
        }
        if (activeReservationService.existsById(id)) {
            boolean isSlotFull = activeReservationService.existsBySlotId(slot.getId(), id);
            return changeActiveReservation(id, command, slot, isSlotFull);
        }
        throw new ReservationNotFoundException("해당 예약을 찾을 수 없습니다.");
    }

    private ReservationInfo changePendingReservation(final Long id, final ReservationChangeCommand command,
                                                     final TimeSlot slot, final boolean isSlotFull) {
        if (isSlotFull) {
            return pendingReservationService.change(id, slot, command.name());
        }
        pendingReservationService.cancel(id, command.name());
        return activeReservationService.transferReservation(id, slot, command.toCreateCommand());
    }

    private ReservationInfo changeActiveReservation(final Long id, final ReservationChangeCommand command,
                                                    final TimeSlot slot, final boolean isSlotFull) {
        if (isSlotFull) {
            return fallbackToPending(id, command, slot);
        }
        Long oldSlotId = activeReservationService.getSlotId(id);
        ReservationInfo changedInfo;
        try {
            changedInfo = activeReservationService.change(id, slot, command.name());
        } catch (ReservationInUseException e) {
            return fallbackToPending(id, command, slot);
        }
        if (!oldSlotId.equals(slot.getId())) {
            promoteNextPending(oldSlotId);
        }
        return changedInfo;
    }

    private ReservationInfo fallbackToPending(final Long id, final ReservationChangeCommand command, final TimeSlot slot) {
        Long oldSlotId = activeReservationService.cancel(id, command.name());
        promoteNextPending(oldSlotId);
        return pendingReservationService.transferReservation(id, slot, command.toCreateCommand());
    }

    private void promoteNextPending(final Long oldSlotId) {
        pendingReservationService.popNextPendingAndPromote(oldSlotId)
                .ifPresent(activeReservationService::savePromoted);
    }
}
