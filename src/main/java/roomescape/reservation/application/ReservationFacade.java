package roomescape.reservation.application;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.TimeSlot;
import roomescape.theme.application.ThemeService;
import roomescape.theme.domain.Theme;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.domain.ReservationTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationFacade {

    private final ActiveReservationService activeReservationService;
    private final PendingReservationService pendingReservationService;
    private final ReservationTimeService timeService;
    private final ThemeService themeService;
    private final TimeSlotService timeSlotService;

    @Transactional(readOnly = true)
    public List<ReservationInfo> getReservations() {
        List<ReservationInfo> activeReservations = activeReservationService.getReservations();
        List<ReservationInfo> pendingReservations = pendingReservationService.getReservations();
        return Stream.concat(activeReservations.stream(), pendingReservations.stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationPendingInfo> getReservationsByName(final String username) {
        List<ReservationPendingInfo> activeReservations = activeReservationService.getReservationsByName(username);
        List<ReservationPendingInfo> pendingReservations = pendingReservationService.getReservationsByName(username);
        return Stream.concat(activeReservations.stream(), pendingReservations.stream())
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationInfo addReservation(final ReservationCreateCommand command) {
        ReservationTime time = timeService.getTime(command.timeId(), command.date());
        Theme theme = themeService.getThemeById(command.themeId());
        TimeSlot slot = timeSlotService.getTimeSlot(command.date(), time, theme);
        try {
            return activeReservationService.add(slot, command);
        } catch (ReservationInUseException e) {
            return pendingReservationService.add(slot, command);
        }
    }

    public void cancelReservation(final Long id, final ReservationCancelCommand command) {
        if (command.status().equals(Status.PENDING)) {
            pendingReservationService.cancel(id, command.name());
            return;
        }
        Long slotId = activeReservationService.cancel(id, command.name());
        pendingReservationService.popNextPendingAndPromote(slotId)
                .ifPresent(activeReservationService::savePromoted);
    }

    @Transactional(propagation = Propagation.NESTED)
    public ReservationInfo changeReservation(final Long id, final ReservationChangeCommand command) {
        ReservationTime time = timeService.getTime(command.timeId(), command.date());
        Theme theme = themeService.getThemeById(command.themeId());
        TimeSlot slot = timeSlotService.getTimeSlot(command.date(), time, theme);

        boolean isSlotFull = activeReservationService.existsBySlotId(slot.getId(), id);

        if (command.status().equals(Status.PENDING)) {
            isSlotFull = activeReservationService.existsBySlotId(slot.getId());
            return changePendingReservation(id, command, slot, isSlotFull);
        }
        return changeActiveReservation(id, command, slot, isSlotFull);
    }

    private ReservationInfo changePendingReservation(final Long id, final ReservationChangeCommand command,
                                                     final TimeSlot slot, final boolean isSlotFull) {
        if (isSlotFull) {
            return pendingReservationService.change(id, slot, command.name());
        }
        pendingReservationService.cancel(id, command.name());
        return activeReservationService.add(slot, command.toCreateCommand());
    }

    private ReservationInfo changeActiveReservation(final Long id, final ReservationChangeCommand command,
                                                    final TimeSlot slot, final boolean isSlotFull) {
        if (isSlotFull) {
            return fallbackToPending(id, command, slot);
        }
        try {
            Long oldSlotId = activeReservationService.getSlotId(id);
            ReservationInfo changedInfo = activeReservationService.change(id, slot, command.name());
            if (!oldSlotId.equals(slot.getId())) {
                pendingReservationService.popNextPendingAndPromote(oldSlotId)
                        .ifPresent(activeReservationService::savePromoted);
            }
            return changedInfo;
        } catch (ReservationInUseException e) {
            return fallbackToPending(id, command, slot);
        }
    }

    private ReservationInfo fallbackToPending(Long id, ReservationChangeCommand command, TimeSlot slot) {
        Long oldSlotId = activeReservationService.cancel(id, command.name());
        pendingReservationService.popNextPendingAndPromote(oldSlotId)
                .ifPresent(activeReservationService::savePromoted);
        return pendingReservationService.add(slot, command.toCreateCommand());
    }
}
