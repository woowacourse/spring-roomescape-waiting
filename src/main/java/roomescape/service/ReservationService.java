package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.repository.PaymentOrderRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.domain.vo.LockedReservationSlots;
import roomescape.domain.vo.ReservationDeletion;
import roomescape.domain.vo.ReservationUpdate;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Service
public class ReservationService {
    private static final int MAX_ORDER_SAVE_ATTEMPTS = 3;

    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    public ReservationService(ReservationSlotRepository reservationSlotRepository, ReservationQueryRepository reservationQueryRepository, PaymentOrderRepository paymentOrderRepository) {
        this.reservationSlotRepository = reservationSlotRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Transactional
    public ReservationResponse save(LocalDateTime now, ReservationRequest request) {
        Long reservationSlotId = getOrCreateReservationSlotId(request);

        ReservationSlot reservationSlot = reservationSlotRepository.findByIdForUpdate(reservationSlotId);
        Reservation newReservation = reservationSlot.reserve(request.name(), now);

        int order = reservationSlot.calculateOrder(newReservation);
        newReservation = reservationSlotRepository.saveReservation(newReservation);
        PaymentOrder paymentOrder = null;
        if (newReservation.isPaymentPending()) {
            paymentOrder = saveOrderWithRetry(newReservation.getId(), calculateAmount(reservationSlot));
        }
        return ReservationResponse.from(newReservation, reservationSlot.getSlot(), order, paymentOrder);
    }

    @Transactional
    public void update(Long reservationId, LocalDateTime now, ReservationRequest request) {
        Long currentSlotId = reservationSlotRepository.findSlotIdByReservationId(reservationId);
        Long newSlotId = getOrCreateReservationSlotId(request);

        LockedReservationSlots lockedSlots = findBothSlotsForUpdate(currentSlotId, newSlotId);

        ReservationSlot currentSlot = lockedSlots.currentSlot();
        ReservationSlot newSlot = lockedSlots.newSlot();

        validateSameTheme(currentSlot.getSlot().theme(), newSlot.getSlot().theme());

        ReservationUpdate promotedReservation = currentSlot.moveOut(reservationId, request.name(), now);
        Reservation updatedReservation = newSlot.moveIn(promotedReservation.updatedReservation(), request.name(), now);

        reservationSlotRepository.updateReservation(updatedReservation);
        promotedReservation.promotedReservation()
                .ifPresent(reservation -> updatePromotedReservation(currentSlot, reservation));
    }

    @Transactional
    public void delete(LocalDateTime now, Long reservationId, String name) {
        ReservationSlot currentSlot = reservationSlotRepository.findByReservationIdForUpdate(reservationId);
        ReservationDeletion promotedReservation = currentSlot.deleteReservation(reservationId, name, now);
        reservationSlotRepository.updateReservation(promotedReservation.deletedReservation());
        promotedReservation.promotedReservation()
                .ifPresent(reservation -> updatePromotedReservation(currentSlot, reservation));
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationQueryRepository.findByUserName(username);
    }

    private Long getOrCreateReservationSlotId(ReservationRequest request) {
        try {
            Optional<Long> reservationSlotId = reservationSlotRepository.findIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
            return reservationSlotId.orElseGet(() ->
                    reservationSlotRepository.save(request.date(), request.timeId(), request.themeId())
            );
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION_SLOT);
        }
    }

    private void validateSameTheme(Theme reservationTheme, Theme newReservationTheme) {
        if (!reservationTheme.equals(newReservationTheme)) {
            throw new CustomException(ErrorCode.UNALLOWED_CHANGE_RESERVATION_THEME);
        }
    }

    private Long calculateAmount(ReservationSlot reservationSlot) {
        return reservationSlot.getSlot().theme().getAmount();
    }

    private void updatePromotedReservation(ReservationSlot reservationSlot, Reservation reservation) {
        reservationSlotRepository.updateReservation(reservation);
        if (reservation.isPaymentPending()) {
            saveOrderWithRetry(reservation.getId(), calculateAmount(reservationSlot));
        }
    }

    private PaymentOrder saveOrderWithRetry(Long reservationId, Long amount) {
        for (int attempt = 0; attempt < MAX_ORDER_SAVE_ATTEMPTS; attempt++) {
            try {
                PaymentOrder paymentOrder = PaymentOrder.create(reservationId, amount);
                paymentOrderRepository.save(paymentOrder);
                return paymentOrder;
            } catch (DuplicateKeyException e) {
                if (attempt == MAX_ORDER_SAVE_ATTEMPTS - 1) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("결제 주문 정보를 저장할 수 없습니다.");
    }

    private LockedReservationSlots findBothSlotsForUpdate(Long currentSlotId, Long newSlotId) {
        Long firstLockId = Math.min(currentSlotId, newSlotId);
        Long secondLockId = Math.max(currentSlotId, newSlotId);

        ReservationSlot first = reservationSlotRepository.findByIdForUpdate(firstLockId);
        ReservationSlot second = reservationSlotRepository.findByIdForUpdate(secondLockId);

        if (currentSlotId.equals(firstLockId)) {
            return new LockedReservationSlots(first, second);
        }

        return new LockedReservationSlots(second, first);
    }

}
