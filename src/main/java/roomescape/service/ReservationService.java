package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.List;
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
public class ReservationService {
    private final SlotService slotService;
    private final ReservationRepository reservationRepository;

    public ReservationService(SlotService slotService, ReservationRepository reservationRepository) {
        this.slotService = slotService;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public RankedReservation reserve(ReservationCreateRequest request, LocalDateTime now) {
        Slot foundSlot = slotService.findOrCreate(request.getDate(), request.getTimeId(), request.getThemeId());
        slotService.lockSlot(foundSlot);

        Reservations reservations = new Reservations(reservationRepository.findAllBySlot(foundSlot));
        Reservation reservation = reservations.reserve(new ReservationName(request.getName()), foundSlot, now);

        return getRankedReservation(reservationRepository.save(reservation));
    }

    private RankedReservation getRankedReservation(Reservation target) {
        return RankedReservation.decideRankFrom(target, reservationRepository.findAllBySlot(target.getSlot()));
    }

    public RankedReservation find(long reservationId) {
        return getRankedReservation(findReservationById(reservationId));
    }

    public List<RankedReservation> findList(String name) {
        Reservations rankedReservations = new Reservations(reservationRepository.findAll());

        if (name == null) {
            return rankedReservations.allRankedReservationsOf();
        }
        return rankedReservations.rankedReservationsOf(name);
    }

    @Transactional
    public RankedReservation update(ReservationUpdateRequest request, long id, LocalDateTime now) {
        Reservation originReservation = findReservationById(id);
        originReservation.ensureNotPast(now);

        Slot updateSlot = slotService.findOrCreate(request.getDate(), request.getTimeId(), request.getThemeId());
        slotService.lockSlot(updateSlot);

        Reservations reservations = new Reservations(reservationRepository.findAllBySlot(updateSlot));
        Reservation reserved = reservations.reserve(new ReservationName(request.getName()), updateSlot, now);

        Reservation updated = reservationRepository.update(id, reserved);

        if (originReservation.isApproved()) {
            findFirstWaitingAndUpdateStatus(originReservation);
        }

        return getRankedReservation(updated);
    }

    private void findFirstWaitingAndUpdateStatus(Reservation reservation) {
        reservationRepository.findFirstWaitingBySlot(reservation.getSlot())
                .ifPresent(waiting -> reservationRepository.updateStatus(waiting.getId(), Status.APPROVED));
    }

    @Transactional
    public void cancel(long reservationId, LocalDateTime now) {
        Reservation reservation = findReservationById(reservationId);
        reservation.ensureNotPast(now);

        reservationRepository.deleteById(reservationId);

        if (reservation.isApproved()) {
            findFirstWaitingAndUpdateStatus(reservation);
        }
    }

    private Reservation findReservationById(long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
