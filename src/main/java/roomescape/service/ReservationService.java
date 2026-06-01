package roomescape.service;

import java.util.ArrayList;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.MyReservationResponse;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;
import roomescape.domain.WaitingReservation;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeSlotRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static roomescape.global.exception.ErrorCode.RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ThemeSlotRepository themeSlotRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ThemeSlotRepository themeSlotRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.themeSlotRepository = themeSlotRepository;
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation saveReservation(String name, Long themeSlotId) {
        ThemeSlot themeSlot = getThemeSlotOrElseThrow(themeSlotId);
        validateBeforeDate(themeSlot);
        validateDuplicatedReservation(name, themeSlotId);
        validateDateTime(themeSlot);
        Reservation reservation = new Reservation(name, themeSlot);

        // RESERVATION 테이블에 ThemeSlot id가 없다면, 바로 themeSlot은 true로, reservation을 confirm로 변경 후 저장
        if (!reservationRepository.existsByThemeSlotId(themeSlotId)) {
            themeSlot.reserve();
            themeSlotRepository.update(themeSlot);
            reservation.confirm();
        }

        // RESERVATION 테이블에 ThemeSlot id가 있다면, reservation을 pending 상태로 바로 저장
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void removeReservation(long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        Long themeSlotId = reservation.getThemeSlotId();
        reservationRepository.deleteById(reservationId);
        boolean hasActiveReservation = reservationRepository.existsByThemeSlotId(themeSlotId);
        themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), hasActiveReservation));
    }

    public Reservation findReservation(long reservationId) {
        return getReservationOrElseThrow(reservationId);
    }

    public MyReservationResponse findReservationBy(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<ReservationResponse> myNotPendingReservation = reservations.stream()
                .filter(reservation -> !reservation.isPendingStatus())
                .map(ReservationResponse::from)
                .toList();

        List<WaitingReservationResponse> waitingReservationResponses = new ArrayList<>();
        // 예약이 PENDING이라면 themeSlot으로 repository에서 대기 순번을 함께 조회한다.
        for (Reservation reservation : reservations) {
            if (reservation.isPendingStatus()) {
                List<WaitingReservation> pendingReservations = findWaitingReservationsWithOrder(reservation.getThemeSlotId());
                WaitingReservationResponse waitingReservationResponse = pendingReservations.stream()
                        .filter(each -> each.name().equals(reservation.getName()))
                        .map(WaitingReservationResponse::from)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다."));

                waitingReservationResponses.add(waitingReservationResponse);
            }
        }
        return new MyReservationResponse(myNotPendingReservation, waitingReservationResponses);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        boolean wasConfirmed = reservation.isConfirmedStatus();

        if (wasConfirmed) {
            themeSlotRepository.findByIdForUpdate(reservation.getThemeSlotId())
                    .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
        }

        reservation.cancel();
        reservationRepository.updateStatus(reservation);

        if (wasConfirmed) {
            promoteWaitingReservationOrReleaseSlot(reservation);
        }
    }

    public void completeReservation(Long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        reservation.complete();
        reservationRepository.updateStatus(reservation);
    }

    @Transactional
    public Reservation modifyReservation(Long reservationId, Long themeSlotId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        validateModifiable(reservation);
        ThemeSlot themeSlot = getThemeSlotOrElseThrow(themeSlotId);

        validateBeforeDate(themeSlot);
        validateDateTime(themeSlot);
        if (reservation.hasDifferentThemeSlot(themeSlotId)) {
            changeThemeSlot(reservation, themeSlotId, themeSlot);
        }

        Reservation updateReservation = new Reservation(
                reservationId,
                reservation.getName(),
                themeSlot,
                reservation.getReservationStatus()
        );
        reservationRepository.updateThemeSlot(updateReservation);
        reservationRepository.updateStatus(updateReservation);
        return updateReservation;
    }

    private void changeThemeSlot(Reservation reservation, Long themeSlotId, ThemeSlot themeSlot) {
        validateIsExistBy(themeSlotId);
        themeSlotRepository.update(new ThemeSlot(themeSlot.getTheme(), themeSlot.getDate(), themeSlot.getTime(), true));

        if (reservation.isConfirmedStatus()) {
            themeSlotRepository.findByIdForUpdate(reservation.getThemeSlotId())
                    .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
            promoteWaitingReservationOrReleaseSlot(reservation);
            return;
        }

        if (reservation.isPendingStatus()) {
            reservation.confirm();
        }
    }

    private void validateModifiable(Reservation reservation) {
        if (!reservation.isModifiableStatus()) {
            throw new CustomException(ErrorCode.INVALID_MODIFY_COMMAND);
        }
    }

    private void promoteWaitingReservationOrReleaseSlot(Reservation reservation) {
        Optional<Reservation> waitingReservation = reservationRepository.findRecentReservationByThemeSlot(reservation.getThemeSlotId());

        if (waitingReservation.isPresent()) {
            waitingReservation.ifPresent(Reservation::confirm);
            reservationRepository.updateStatus(waitingReservation.get());
        }

        if (waitingReservation.isEmpty()) {
            themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false));
        }
    }

    @Transactional(readOnly = true)
    public List<WaitingReservation> findWaitingReservationsWithOrder(Long themeSlotId) {
        return reservationRepository.findWaitingReservationsWithOrder(themeSlotId);
    }

    @NonNull
    private ThemeSlot getThemeSlotOrElseThrow(Long themeSlotId) {
        return themeSlotRepository.findById(themeSlotId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    }

    @NonNull
    private Reservation getReservationOrElseThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateBeforeDate(ThemeSlot themeSlot) {
        if (themeSlot.getDate().isBefore(java.time.LocalDate.now())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED_DATE);
        }
    }

    private void validateIsExistBy(Long themeSlotId) {
        if (reservationRepository.existsByThemeSlotId(themeSlotId)) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_EXIST);
        }
    }

    private void validateDateTime(ThemeSlot themeSlot) {
        if (themeSlot.getDate().equals(java.time.LocalDate.now()) && themeSlot.getTime().isBefore(LocalTime.now())) {
            throw new CustomException(ErrorCode.RESERVATION_TIME_OUT);
        }
    }

    private void validateDuplicatedReservation(String name, Long themeSlotId) {
        if (reservationRepository.existsByThemeSlotIdAndMemberName(name, themeSlotId)) {
            throw new CustomException(RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT);
        }
    }
}
