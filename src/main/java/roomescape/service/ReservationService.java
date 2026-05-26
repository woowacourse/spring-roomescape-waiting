package roomescape.service;

import java.util.ArrayList;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
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
            themeSlot.swtichIsReserved();
            themeSlotRepository.update(themeSlot);
            reservation.confirm();
        }

        // RESERVATION 테이블에 ThemeSlot id가 있다면, reservation을 pending 상태로 바로 저장
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void removeReservation(long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        reservationRepository.deleteById(reservationId);
        themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false));
    }

    public Reservation findReservation(long reservationId) {
        return getReservationOrElseThrow(reservationId);
    }

    public List<Reservation> findReservationBy(String name) {
        return reservationRepository.findByName(name);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        if (reservation.getReservationStatus().equals(PendingStatus.getInstance())) {
            reservation.cancel();
            reservationRepository.updateStatus(reservation);
            return;
        }

        if (reservation.getReservationStatus().equals(ConfirmedStatus.getInstance())) {
            reservation.cancel();
            reservationRepository.updateStatus(reservation);

            Optional<Reservation> waitingReservation = reservationRepository.findRecentReservationByThemeSlot(reservation.getThemeSlot().getId());

            if (waitingReservation.isPresent()) {
                waitingReservation.ifPresent(Reservation::confirm);
                reservationRepository.updateStatus(waitingReservation.get());
            }

            if (waitingReservation.isEmpty()) {
                reservation.getThemeSlot().swtichIsReserved();
                themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false));
            }
        }
    }

    @Transactional
    public Reservation modifyReservation(Long reservationId, Long themeSlotId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        ThemeSlot themeSlot = getThemeSlotOrElseThrow(themeSlotId);

        validateBeforeDate(themeSlot);
        validateDateTime(themeSlot);
        if (!reservation.getThemeSlot().getId().equals(themeSlotId)) {
            validateIsExistBy(themeSlotId);
            themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false));
            themeSlotRepository.update(new ThemeSlot(themeSlot.getTheme(), themeSlot.getDate(), themeSlot.getTime(), true));
        }

        Reservation updateReservation = new Reservation(
                reservationId,
                reservation.getName(),
                themeSlot,
                reservation.getReservationStatus()
        );
        reservationRepository.updateThemeSlot(updateReservation);
        return updateReservation;
    }

    public List<WaitingReservationResponse> findWaitingReservationWithOrder(Long id) {
        List<WaitingReservationResponse> list = new ArrayList<>();
        List<Reservation> reservations = reservationRepository.findByThemeSlotAndPending(id);
        for (int i = 1; i <= reservations.size(); i++) {
            WaitingReservationResponse response = WaitingReservationResponse.from(i, reservations.get(i - 1));
            list.add(response);
        }
        return list;
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
