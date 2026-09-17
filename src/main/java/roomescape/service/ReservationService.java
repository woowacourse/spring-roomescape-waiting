package roomescape.service;

import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.MyReservationResponse;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Waiting;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeSlotRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static roomescape.global.exception.ErrorCode.RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ThemeSlotRepository themeSlotRepository;
    private final WaitingRepository waitingRepository;

    @Autowired
    public ReservationService(
            ReservationRepository reservationRepository,
            ThemeSlotRepository themeSlotRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.themeSlotRepository = themeSlotRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation saveReservation(String name, Long themeSlotId) {
        ThemeSlot themeSlot = getThemeSlotForUpdateOrElseThrow(themeSlotId);
        validateBeforeDate(themeSlot);
        validateDuplicatedReservation(name, themeSlot);
        validateDateTime(themeSlot);
        if (reservationRepository.existsConfirmedByThemeSlotId(themeSlotId)) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_EXIST);
        }

        Reservation reservation = new Reservation(name, themeSlot);
        updateThemeSlotReserved(themeSlot, true);
        reservation.confirm();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void removeReservation(long reservationId) {
        Reservation reservation = getReservationForUpdateOrElseThrow(reservationId);
        boolean wasConfirmed = reservation.isConfirmedStatus();

        if (wasConfirmed) {
            lockFirstWaitingThenThemeSlot(reservation);
        }

        reservationRepository.deleteById(reservationId);

        if (wasConfirmed) {
            reservationRepository.flush();
            promoteWaitingReservationOrReleaseSlot(reservation);
            return;
        }

        boolean hasConfirmedReservation = reservationRepository.existsConfirmedByThemeSlotId(reservation.getThemeSlotId());
        updateThemeSlotReserved(reservation.getThemeSlot(), hasConfirmedReservation);
    }

    @Transactional(readOnly = true)
    public MyReservationResponse findReservationBy(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<ReservationResponse> myReservations = reservations.stream()
                .map(ReservationResponse::from)
                .toList();

        List<WaitingReservationResponse> waitingReservationResponses = waitingRepository.findWithRankByMemberName(name)
                .stream()
                .map(WaitingReservationResponse::from)
                .toList();
        return new MyReservationResponse(myReservations, waitingReservationResponses);
    }

    @Transactional
    public void cancelReservation(Long reservationId, String name) {
        Reservation reservation = getReservationForUpdateOrElseThrow(reservationId);
        validateOwner(reservation, name);
        cancel(reservation);
    }

    private void cancel(Reservation reservation) {
        String expectedStatus = reservation.getReservationStatusName();
        boolean wasConfirmed = reservation.isConfirmedStatus();

        if (wasConfirmed) {
            lockFirstWaitingThenThemeSlot(reservation);
        }

        reservation.cancel();
        updateStatusOrElseThrow(reservation, expectedStatus);

        if (wasConfirmed) {
            reservationRepository.flush();
            promoteWaitingReservationOrReleaseSlot(reservation);
        }
    }

    private void validateOwner(Reservation reservation, String name) {
        if (!reservation.isOwnedBy(name)) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED);
        }
    }

    @Transactional
    public Reservation modifyReservation(Long reservationId, Long themeSlotId) {
        Reservation reservation = getReservationForUpdateOrElseThrow(reservationId);
        validateModifiable(reservation);
        String expectedStatus = reservation.getReservationStatusName();
        ThemeSlot previousThemeSlot = reservation.getThemeSlot();
        ThemeSlot themeSlot = getThemeSlotWithOrderedLock(reservation.getThemeSlotId(), themeSlotId);

        validateBeforeDate(themeSlot);
        validateDateTime(themeSlot);
        boolean changesThemeSlot = reservation.hasDifferentThemeSlot(themeSlotId);
        boolean shouldPromotePreviousSlot = changesThemeSlot && reservation.isConfirmedStatus();
        if (changesThemeSlot) {
            changeThemeSlot(reservation, themeSlotId, themeSlot);
        }

        Reservation updateReservation = new Reservation(
                reservationId,
                reservation.getName(),
                themeSlot,
                reservation.getReservationStatus()
        );
        reservationRepository.updateThemeSlot(updateReservation);
        updateStatusOrElseThrow(updateReservation, expectedStatus);
        if (shouldPromotePreviousSlot) {
            reservationRepository.flush();
            promoteWaitingReservationOrReleaseSlot(previousThemeSlot);
        }
        return updateReservation;
    }

    private void changeThemeSlot(Reservation reservation, Long themeSlotId, ThemeSlot themeSlot) {
        validateDuplicatedReservation(reservation.getName(), themeSlot);
        boolean targetSlotHasConfirmedReservation = reservationRepository.existsConfirmedByThemeSlotId(themeSlotId);
        if (targetSlotHasConfirmedReservation) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_EXIST);
        }

        updateThemeSlotReserved(themeSlot, true);
    }

    private void validateModifiable(Reservation reservation) {
        if (!reservation.isModifiableStatus()) {
            throw new CustomException(ErrorCode.INVALID_MODIFY_COMMAND);
        }
    }

    private void lockFirstWaitingThenThemeSlot(Reservation reservation) {
        waitingRepository.findFirstByThemeAndDateAndTimeOrderByIdAsc(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime()
        );
        themeSlotRepository.findByIdForUpdate(reservation.getThemeSlotId())
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    }

    private void promoteWaitingReservationOrReleaseSlot(Reservation reservation) {
        promoteWaitingReservationOrReleaseSlot(reservation.getThemeSlot());
    }

    private void promoteWaitingReservationOrReleaseSlot(ThemeSlot themeSlot) {
        if (promoteWaiting(themeSlot)) {
            return;
        }
        updateThemeSlotReserved(themeSlot, false);
    }

    private boolean promoteWaiting(ThemeSlot themeSlot) {
        Optional<Waiting> waiting = waitingRepository.findFirstByThemeAndDateAndTimeOrderByIdAsc(
                themeSlot.getTheme(),
                themeSlot.getDate(),
                themeSlot.getTime()
        );
        if (waiting.isEmpty()) {
            return false;
        }

        Waiting firstWaiting = waiting.get();
        Reservation promotedReservation = new Reservation(firstWaiting.getMemberName(), themeSlot);
        promotedReservation.confirm();
        reservationRepository.save(promotedReservation);
        waitingRepository.delete(firstWaiting);
        return true;
    }

    private void updateStatusOrElseThrow(Reservation reservation, String expectedStatus) {
        if (!reservationRepository.updateStatus(reservation, expectedStatus)) {
            throw new CustomException(ErrorCode.RESERVATION_STATUS_CONFLICT);
        }
    }

    @NonNull
    private ThemeSlot getThemeSlotForUpdateOrElseThrow(Long themeSlotId) {
        return themeSlotRepository.findByIdForUpdate(themeSlotId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    }

    @NonNull
    private Reservation getReservationOrElseThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @NonNull
    private Reservation getReservationForUpdateOrElseThrow(long reservationId) {
        return reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @NonNull
    private ThemeSlot getThemeSlotWithOrderedLock(Long currentThemeSlotId, Long targetThemeSlotId) {
        return themeSlotRepository.findAllByIdsForUpdateInOrder(currentThemeSlotId, targetThemeSlotId)
                .stream()
                .filter(themeSlot -> themeSlot.hasSameId(targetThemeSlotId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    }

    private void validateBeforeDate(ThemeSlot themeSlot) {
        if (themeSlot.getDate().isBefore(java.time.LocalDate.now())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED_DATE);
        }
    }

    private void validateDateTime(ThemeSlot themeSlot) {
        if (themeSlot.getDate().equals(java.time.LocalDate.now()) && themeSlot.getTime().isBefore(LocalTime.now())) {
            throw new CustomException(ErrorCode.RESERVATION_TIME_OUT);
        }
    }

    private void validateDuplicatedReservation(String name, ThemeSlot themeSlot) {
        boolean hasReservation = reservationRepository.existsByThemeSlotIdAndMemberName(name, themeSlot.getId());
        boolean hasWaiting = waitingRepository.existsByMemberNameAndThemeAndDateAndTime(
                name,
                themeSlot.getTheme(),
                themeSlot.getDate(),
                themeSlot.getTime()
        );
        if (hasReservation || hasWaiting) {
            throw new CustomException(RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT);
        }
    }

    private void updateThemeSlotReserved(ThemeSlot themeSlot, boolean isReserved) {
        if (isReserved) {
            themeSlot.reserve();
        } else {
            themeSlot.release();
        }
        themeSlotRepository.updateReserved(themeSlot);
    }
}
