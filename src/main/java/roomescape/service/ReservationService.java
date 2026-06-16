package roomescape.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.domain.reservationOrder.ReservationOrderRepository;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationWaiting.ReservationWaitingRepository;
import roomescape.domain.slot.Slot;
import roomescape.domain.slot.SlotDomainService;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReserveResponse;
import roomescape.exception.ConcurrencyConflictException;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final SlotDomainService slotDomainService;
    private final ReservationOrderService reservationOrderService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationWaitingRepository reservationWaitingRepository,
                              SlotDomainService slotDomainService, ReservationOrderRepository reservationOrderRepository,
                              ReservationOrderService reservationOrderService) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.slotDomainService = slotDomainService;
        this.reservationOrderService = reservationOrderService;
    }

    public ReservationResponse read(Long id) {
        return ReservationResponse.from(getReservation(id));
    }

    public List<ReservationResponse> readAll() {
        return reservationRepository.findAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readByName(String name) {
        return reservationRepository.findAllByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReserveResponse reserve(ReservationRequest reservationReq) {
        if (slotDomainService.isExistByDateAndTimeAndTheme(reservationReq.date(), reservationReq.timeId(), reservationReq.themeId())) {
            throw new ReservationAlreadyExistException();
        }
        Slot slot = slotDomainService.create(reservationReq.date(), reservationReq.timeId(), reservationReq.themeId());

        Reservation reservation = Reservation.create(reservationReq.name(), slot);

        Long reservationId = reservationRepository.insert(reservation);
        ReservationOrder order = reservationOrderService.insert(reservationId);

        return ReserveResponse.from(order);
    }

    @Retryable(retryFor = ConcurrencyConflictException.class, backoff = @Backoff(delay = 50, multiplier = 2.0, random = true))
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

    @Retryable(retryFor = ConcurrencyConflictException.class, backoff = @Backoff(delay = 50, multiplier = 2.0, random = true))
    @Transactional
    public void delete(Long id) {
        Optional<Reservation> optionalReservation = reservationRepository.findReservationById(id);
        if (optionalReservation.isEmpty()) {
            return;
        }
        Reservation reservation = optionalReservation.get();
        if (reservation.isExpired()) {
            throw new ExpiredDateTimeException();
        }
        long deletedRow = reservationRepository.delete(reservation.getId());

        if(deletedRow == 0) {
            return;
        }
        promoteOrCleanupSlot(reservation.getSlot().getId());
    }

    private ReservationResponse updateReservation(Reservation existedReservation, ReservationRequest request) {
        Reservation updated = existedReservation.update(request.name());
        if (reservationWaitingRepository.isExistByNameAndSlotId(updated.getName(), updated.getSlotId())) {
            throw new InvalidInputException("이미 대기열로 존재합니다.");
        }
        reservationRepository.updateName(updated.getId(), request.name());
        return ReservationResponse.from(updated);
    }

    private void promoteOrCleanupSlot(Long slotId) {
        Optional<ReservationWaiting> firstWaiting = reservationWaitingRepository.findFirstBySlotId(slotId);
        if (firstWaiting.isEmpty()) {
            slotDomainService.delete(slotId);
            return;
        }

        promoteWaiting(firstWaiting.get());
    }

    private void promoteWaiting(ReservationWaiting reservationWaiting) {
        long claimed = reservationWaitingRepository.delete(reservationWaiting.getId());
        if (claimed == 0) {
            throw new ConcurrencyConflictException("승격 대상 대기가 사라졌습니다.");
        }

        reservationRepository.insert(reservationWaiting.promote());
    }

    private Reservation updateSlot(Reservation existed, ReservationRequest request) {
        if (slotDomainService.isExistByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId())) {
            throw new ReservationAlreadyExistException();
        }

        Slot newSlot = slotDomainService.create(request.date(), request.timeId(), request.themeId());
        Reservation moved = existed.update(request.name(), newSlot);

        if (reservationRepository.isExistBySlot(newSlot.getId())) {
            throw new ReservationAlreadyExistException();
        }

        reservationRepository.update(moved.getId(), moved.getName(), newSlot.getId(), moved.getCreatedAt());
        return moved;
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약을 찾을 수 없습니다."));
    }
}
