package roomescape.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.MyReservationResponse;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeSlotRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


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
        ThemeSlot themeSlot = themeSlotRepository.findWithReservations(themeSlotId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
        validateBeforeDate(themeSlot);
        validateDateTime(themeSlot);
        String orderId = UUID.randomUUID().toString();
        Long amount = themeSlot.getTheme().getPrice();
        Reservation reservation = themeSlot.addReservation(name, orderId, amount);
        themeSlotRepository.update(themeSlot);
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

    public MyReservationResponse findReservationBy(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<ReservationResponse> myNotPendingReservation = reservations.stream()
                .filter(reservation -> !reservation.isPendingStatus())
                .map(ReservationResponse::from)
                .toList();

        List<WaitingReservationResponse> waitingReservationResponses = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.isPendingStatus()) {
                ThemeSlot themeSlot = themeSlotRepository.findWithReservations(reservation.getThemeSlotId())
                        .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
                int order = themeSlot.getReservations().waitingOrderOf(reservation.getId());
                waitingReservationResponses.add(WaitingReservationResponse.from(order, reservation));
            }
        }
        return new MyReservationResponse(myNotPendingReservation, waitingReservationResponses);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        ThemeSlot themeSlot = themeSlotRepository.findWithReservations(reservation.getThemeSlotId())
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));

        Optional<Reservation> promotedReservation = themeSlot.cancelReservation(reservationId);
        reservationRepository.updateStatus(themeSlot.findReservationById(reservationId));
        promotedReservation.ifPresent(reservationRepository::updateStatus);
        themeSlotRepository.update(themeSlot);
    }

    public void completeReservation(Long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        reservation.complete();
        reservationRepository.updateStatus(reservation);
    }

    @Transactional
    public Reservation modifyReservation(Long reservationId, Long themeSlotId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        ThemeSlot themeSlot = getThemeSlotOrElseThrow(themeSlotId);

        validateBeforeDate(themeSlot);
        validateDateTime(themeSlot);
        if (!reservation.getThemeSlotId().equals(themeSlotId)) {
            validateIsExistBy(themeSlotId);
            themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false));
            themeSlotRepository.update(new ThemeSlot(themeSlot.getTheme(), themeSlot.getDate(), themeSlot.getTime(), true));
        }

        Reservation updateReservation = new Reservation(
                reservationId,
                reservation.getName(),
                themeSlot.getId(),
                themeSlot.getDate(),
                themeSlot.getTime(),
                themeSlot.getTheme(),
                reservation.getReservationStatus()
        );
        reservationRepository.updateThemeSlot(updateReservation);
        return updateReservation;
    }

    public List<WaitingReservationResponse> findWaitingReservationWithOrder(Long themeSlotId) {
        ThemeSlot themeSlot = themeSlotRepository.findWithReservations(themeSlotId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
        List<Reservation> pending = themeSlot.getReservations().pendingByOrder();
        List<WaitingReservationResponse> list = new ArrayList<>();
        for (int i = 0; i < pending.size(); i++) {
            list.add(WaitingReservationResponse.from(i + 1, pending.get(i)));
        }
        return list;
    }

    private ThemeSlot getThemeSlotOrElseThrow(Long themeSlotId) {
        return themeSlotRepository.findById(themeSlotId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    }

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
}
