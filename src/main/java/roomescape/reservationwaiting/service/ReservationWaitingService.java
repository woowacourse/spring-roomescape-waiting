package roomescape.reservationwaiting.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.ReservationSlot;
import roomescape.common.event.ReservationEvent;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingFactory reservationWaitingFactory;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository,
                                     ReservationWaitingFactory reservationWaitingFactory,
                                     Clock clock) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationWaitingFactory = reservationWaitingFactory;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaitingResponse createWaiting(ReservationWaitingRequest request) {
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESERVATION_NOT_FOUND));
        if (reservationWaitingRepository.isWaitingBy(reservation.getSlot(), request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAITING);
        }
        if (reservationRepository.isReservedBy(reservation.getSlot(), request.name())) {
            throw new BusinessException(ErrorCode.WAITING_ON_OWN_RESERVATION);
        }
        try {
            ReservationWaiting waiting = reservationWaitingRepository.save(
                    reservationWaitingFactory.create(request.name(), reservation));
            return ReservationWaitingResponse.from(waiting);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAITING);
        }
    }

    @Transactional
    public void deleteWaiting(Long id) {
        ReservationWaiting waiting = getById(id);
        waiting.validateCancelable(clock, ErrorCode.PAST_WAITING_CANCEL);
        reservationWaitingRepository.deleteById(id);
    }

    @Transactional
    public List<ReservationWaitingTurnResponse> getWaitingByName(String name) {
        List<ReservationWaiting> reservationWaitings = reservationWaitingRepository.findByName(name);
        Map<Long, Long> turns = reservationWaitingRepository.calculateTurn(name);

        return reservationWaitings.stream()
                .map(waiting -> ReservationWaitingTurnResponse.from(waiting, turns.get(waiting.getId())))
                .toList();
    }

    @Transactional
    @EventListener
    public void promoteWaiting(ReservationEvent event) {
        reservationWaitingRepository.findOldestBySlot(event.getSlot())
                .ifPresent(waiting -> {
                    try {
                        reservationRepository.save(waiting.toReservation());
                    } catch (DuplicateKeyException e) {
                        throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
                    }
                    reservationWaitingRepository.deleteById(waiting.getId());
                });
    }

    @Transactional
    public ReservationResponse approveWaiting(Long waitingId) {
        ReservationWaiting waiting = getById(waitingId);
        ReservationSlot slot = waiting.getSlot();
        if (reservationRepository.isBooked(slot)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }
        reservationWaitingRepository.deleteById(waiting.getId());

        try {
            return ReservationResponse.from(
                    reservationRepository.save(waiting.toReservation()));
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @NonNull
    private ReservationWaiting getById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_NOT_FOUND));
    }
}
