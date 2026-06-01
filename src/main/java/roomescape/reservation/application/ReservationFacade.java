package roomescape.reservation.application;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
            pendingReservationService.cancel(id, command);
            return;
        }
        Long slotId = activeReservationService.cancel(id, command);
        pendingReservationService.popNextPendingAndPromote(slotId)
                .ifPresent(activeReservationService::savePromoted);
    }

    public ReservationInfo changeReservation(final Long id, final ReservationChangeCommand command) {
        ReservationTime time = timeService.getTime(command.timeId(), command.date());
        Theme theme = themeService.getThemeById(command.themeId());
        TimeSlot slot = timeSlotService.getTimeSlot(command.date(), time, theme);

        boolean isSlotFull = activeReservationService.existsBySlotId(slot.getId());

        if (command.status().equals(Status.PENDING)) {
            return changePendingReservation(id, command, slot, isSlotFull);
        }
        return changeActiveReservation(id, command, slot, isSlotFull);
    }

    private ReservationInfo changePendingReservation(final Long id, final ReservationChangeCommand command, final TimeSlot slot, final boolean isSlotFull) {
        if (isSlotFull) {
            return pendingReservationService.change(id, slot, command);
        }
        pendingReservationService.cancel(id, command.toCancelCommand());
        return activeReservationService.add(slot, command.toCreateCommand());
    }

    private ReservationInfo changeActiveReservation(final Long id, final ReservationChangeCommand command, final TimeSlot slot, final boolean isSlotFull) {
        if (isSlotFull) {
            Long oldSlotId = activeReservationService.cancel(id, command.toCancelCommand());
            pendingReservationService.popNextPendingAndPromote(oldSlotId)
                    .ifPresent(activeReservationService::savePromoted);
            return pendingReservationService.add(slot, command.toCreateCommand());
        }
        Long oldSlotId = activeReservationService.getSlotId(id);
        ReservationInfo changedInfo = activeReservationService.change(id, slot, command);
        pendingReservationService.popNextPendingAndPromote(oldSlotId)
                .ifPresent(activeReservationService::savePromoted);
        return changedInfo;
    }

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
}
