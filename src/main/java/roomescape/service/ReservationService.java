package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.slot.Slot;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.ConcurrencyConflictException;
import roomescape.exception.ExpiredDateTimeException;
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

    @Retryable(retryFor = {ConcurrencyConflictException.class, PessimisticLockingFailureException.class},
             backoff = @Backoff(delay = 50, multiplier = 2.0, random = true))
    @Transactional
    public ReservationResponse create(ReservationRequest reservationReq) {
        try {
            Slot slot = slotService.getOrCreate(reservationReq);
            Reservation reservation = Reservation.create(reservationReq.name(), slot);
            Long reservationId = reservationUpdatingDao.insert(reservation);
            return ReservationResponse.from(reservation.withId(reservationId));
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistException();
        }
    }

    @Retryable(retryFor = {ConcurrencyConflictException.class, PessimisticLockingFailureException.class},
            backoff = @Backoff(delay = 50, multiplier = 2.0, random = true))
    @Transactional
    public ReservationResponse update(Long id, ReservationRequest reservationRequest) {
        Reservation existed = getReservation(id);

        if (existed.isSameSlot(reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId())) {
            Reservation updated = existed.update(reservationRequest.name());
            reservationUpdatingDao.updateName(id, reservationRequest.name());
            return ReservationResponse.from(updated);
        }

        try {
            Long previousSlotId = existed.getSlotId();
            Reservation moved = updateSlot(existed, reservationRequest);
            promoteOrCleanupSlot(previousSlotId);
            return ReservationResponse.from(moved);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistException();
        }
    }

    @Retryable(retryFor = {ConcurrencyConflictException.class, PessimisticLockingFailureException.class},
            backoff = @Backoff(delay = 50, multiplier = 2.0, random = true))
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

    private void promoteOrCleanupSlot(Long slotId) {
        Optional<ReservationWaiting> firstWaiting = reservationWaitingDao.findFirstBySlotId(slotId);
        if (firstWaiting.isEmpty()) {
            try {
                slotService.delete(slotId);
            } catch (DataIntegrityViolationException e) {
                throw new ConcurrencyConflictException("대기열이 변경되었습니다. 다시 시도해주세요.");
            }
            return;
        }

        promoteWaiting(firstWaiting.get());
    }

    private void promoteWaiting(ReservationWaiting reservationWaiting) {
        long claimed = reservationWaitingDao.delete(reservationWaiting.getId());
        if (claimed == 0) {
            throw new ConcurrencyConflictException("대기열이 변경되었습니다. 다시 시도해주세요.");
        }
        reservationUpdatingDao.insert(reservationWaiting.promote());
    }

    private Reservation updateSlot(Reservation existed, ReservationRequest request) {
        Slot newSlot = slotService.getOrCreate(request);

        if (reservationQueryingDao.isExistBySlot(newSlot.getId())) {
            throw new ReservationAlreadyExistException();
        }

        Reservation moved = existed.update(request.name(), newSlot);
        long updated = reservationUpdatingDao.update(moved.getId(), moved.getName(), newSlot.getId(), moved.getCreatedAt());
        if (updated == 0) {
            throw new ResourceNotFoundException("해당 예약이 존재하지 않습니다.");
        }
        return moved;
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약을 찾을 수 없습니다."));
    }
}
