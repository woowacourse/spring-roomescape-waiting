package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.RankedReservation;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {
    private final SlotService slotService;
    private final ReservationRepository reservationRepository;

    @Transactional
    public RankedReservation reserve(ReservationCreateRequest request, LocalDateTime now) {
        Slot foundSlot = slotService.findOrCreate(request.getDate(), request.getTimeId(), request.getThemeId());

        Reservations reservations = new Reservations(reservationRepository.findAllBySlot(foundSlot));
        Reservation reservation = reservations.reserve(new ReservationName(request.getName()), foundSlot, now);

        validateIsPastReservation(reservation, now);

        return getRankedReservation(reservationRepository.save(reservation));
    }

    private RankedReservation getRankedReservation(Reservation target) {
        return RankedReservation.decideRankFrom(target, reservationRepository.findAllBySlot(target.getSlot()));
    }

    public RankedReservation find(long reservationId) {
        return getRankedReservation(findReservationById(reservationId));
    }

    public List<RankedReservation> findList(String name) {
        Reservations reservations = new Reservations(reservationRepository.findAll());

        return reservations.rankedReservationsOf(name);
    }

    @Transactional
    public RankedReservation update(ReservationUpdateRequest request, long id, LocalDateTime now) {
        Reservation originReservation = findReservationById(id);

        validateIsPastReservation(originReservation, now);

        Slot originSlot = originReservation.getSlot();
        boolean wasApproved = originReservation.isApproved();

        Slot updateSlot = slotService.findOrCreate(request.getDate(), request.getTimeId(), request.getThemeId());

        Reservations reservations = new Reservations(reservationRepository.findAllBySlot(updateSlot));
        Reservation reserved = reservations.reserve(new ReservationName(request.getName()), updateSlot, now);

        originReservation.changeTo(reserved);

        if (wasApproved) {
            promoteFirstWaiting(originSlot);
        }

        return getRankedReservation(originReservation);
    }

    private void validateIsPastReservation(Reservation reservation, LocalDateTime now) {
        if (reservation.isPastThan(now)) {
            throw new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    private void promoteFirstWaiting(Slot slot) {
        reservationRepository.findFirstBySlotAndStatusOrderByCreatedAtAscIdAsc(slot, Status.WAITING)
                .ifPresent(Reservation::approve);
    }

    @Transactional
    public void cancel(long reservationId, LocalDateTime now) {
        Reservation reservation = findReservationById(reservationId);

        validateIsPastReservation(reservation, now);

        reservationRepository.deleteById(reservationId);

        if (reservation.isApproved()) {
            promoteFirstWaiting(reservation.getSlot());
        }
    }

    private Reservation findReservationById(long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
