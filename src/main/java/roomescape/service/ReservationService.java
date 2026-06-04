package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.slot.Slot;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.ConcurrencyConflictException;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationUpdatingDao;
import roomescape.repository.ReservationWaitingDao;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationQueryingDao reservationQueryingDao;
    private final ReservationUpdatingDao reservationUpdatingDao;
    private final ReservationWaitingDao reservationWaitingDao;
    private final SlotService slotService;

    public ReservationService(ReservationQueryingDao reservationQueryingDao, ReservationUpdatingDao reservationUpdatingDao,
                              ReservationWaitingDao reservationWaitingDao, SlotService slotService) {
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationUpdatingDao = reservationUpdatingDao;
        this.reservationWaitingDao = reservationWaitingDao;
        this.slotService = slotService;
    }

    public ReservationResponse read(Long id) {
        return ReservationResponse.from(getReservation(id));
    }

    public List<ReservationResponse> readAll() {
        return reservationQueryingDao.findAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readByName(String name) {
        return reservationQueryingDao.findAllByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse create(ReservationRequest reservationReq) {
        if(slotService.isExistByDateAndTimeAndTheme(reservationReq.date(), reservationReq.timeId(), reservationReq.themeId())) {
            throw new ReservationAlreadyExistException();
        }
        Slot slot = slotService.create(reservationReq.date(), reservationReq.timeId(), reservationReq.themeId());

        if (slot.isExpired()) {
            throw new ExpiredDateTimeException();
        }

        Reservation reservation = Reservation.create(reservationReq.name(), slot);
        Long reservationId = reservationUpdatingDao.insert(reservation);
        return ReservationResponse.from(reservation.withId(reservationId));
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationRequest reservationRequest) {
        Reservation existed = getReservation(id);

        if (existed.isSameSlot(reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId())) {
            return updateReservation(existed, reservationRequest);
        }

        Long previousSlotId = existed.getSlotId();
        Reservation moved = updateSlot(existed, reservationRequest);
        promoteOrCleanupSlot(previousSlotId);
        return ReservationResponse.from(moved);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Reservation> optionalReservation = reservationQueryingDao.findReservationById(id);
        if (optionalReservation.isEmpty()) {
            return;
        }
        Reservation reservation = optionalReservation.get();
        if (reservation.isExpired()) {
            throw new ExpiredDateTimeException();
        }
        reservationUpdatingDao.delete(reservation.getId());
        promoteOrCleanupSlot(reservation.getSlot().getId());
    }

    private ReservationResponse updateReservation(Reservation existedReservation, ReservationRequest request) {
        Reservation updated = existedReservation.update(request.name());
        if(reservationWaitingDao.isExistByNameAndSlotId(updated.getName(), updated.getSlotId())) {
            throw new InvalidInputException("이미 대기열로 존재합니다.");
        }
        reservationUpdatingDao.updateName(updated.getId(), request.name());
        return ReservationResponse.from(updated);
    }

    private void promoteOrCleanupSlot(Long slotId) {
        Optional<ReservationWaiting> firstWaiting = reservationWaitingDao.findFirstBySlotId(slotId);
        if (firstWaiting.isEmpty()) {
            slotService.delete(slotId);
            return;
        }

        promoteWaiting(firstWaiting.get());
    }

    private void promoteWaiting(ReservationWaiting reservationWaiting) {
        if(!reservationWaitingDao.isExistByNameAndSlotId(reservationWaiting.getName(), reservationWaiting.getSlot().getId())) {
            throw new ConcurrencyConflictException("승격할 대기가 존재하지 않습니다.");
        }

        reservationWaitingDao.delete(reservationWaiting.getId());
        reservationUpdatingDao.insert(reservationWaiting.promote());
    }

    private Reservation updateSlot(Reservation existed, ReservationRequest request) {
        if(slotService.isExistByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId())) {
            throw new ReservationAlreadyExistException();
        }

        Slot newSlot = slotService.create(request.date(), request.timeId(), request.themeId());
        Reservation moved = existed.update(request.name(), newSlot);

        if (reservationQueryingDao.isExistBySlot(newSlot.getId())) {
            throw new ReservationAlreadyExistException();
        }

        reservationUpdatingDao.update(moved.getId(), moved.getName(), newSlot.getId(), moved.getCreatedAt());
        return moved;
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약을 찾을 수 없습니다."));
    }
}
