package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ResourceInUseException;
import roomescape.order.dao.dto.OrderRow;
import roomescape.order.service.OrderService;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.dto.request.ConfirmRequest;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeFindAllResponse;

@Service
public class ReservationFacade {

    public static final long DEFAULT_RESERVATION_PRICE = 50000L;

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    private final ConcurrentHashMap<String, ReentrantLock> slotLocks = new ConcurrentHashMap<>();

    public ReservationFacade(ReservationService reservationService,
        ReservationTimeService reservationTimeService, OrderService orderService,
        PaymentService paymentService) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public ReservationCreateResponse createReservation(ReservationRequest request) {
        Lock lock = getSlotLock(LocalDate.parse(request.date()), request.timeId(),
            request.themeId());
        lock.lock();
        try {
            ReservationCreateResponse reservationCreateResponse = reservationService.create(
                request);

            if (reservationCreateResponse.status() == ReservationStatus.PENDING) {
                String orderId = generateOrderId();
                orderService.save(reservationCreateResponse.id(), orderId, DEFAULT_RESERVATION_PRICE);
                return reservationCreateResponse.withOrder(orderId, DEFAULT_RESERVATION_PRICE);
            }
            return reservationCreateResponse;
        } finally {
            lock.unlock();
        }
    }

    public void deleteReservationTime(Long id) {
        if (reservationService.existsByTimeId(id)) {
            throw new ResourceInUseException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }

        reservationTimeService.delete(id);
    }

    public ReservationTimeCreateResponse createReservationTime(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        return reservationTimeService.create(reservationTimeCreateRequest);
    }

    public List<ReservationTimeFindAllResponse> findAllReservationTime() {
        return reservationTimeService.findAll();
    }

    public List<ReservationResponse> findAllReservation() {
        return reservationService.findAll();
    }

    public void deleteReservation(Long id) {
        reservationService.delete(id);
    }

    public ReservationResponse findReservationById(Long id) {
        return reservationService.findById(id);
    }

    public void deleteReservedByNameAndReservationId(String name, Long reservationId) {
        ReservationResponse info = reservationService.findById(reservationId);
        Lock lock = getSlotLock(info.date(), info.time().id(), info.theme().id());
        lock.lock();
        try {
            Reservation reservation = reservationService.deleteMyReservation(reservationId, name);
            try {
                reservationService.promoteFirstWaiting(reservation);
            } catch (Exception e) {
                reservationService.restoreReservation(reservation.getId());
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    public void deleteWaitingByNameAndReservationId(String name, Long reservationId) {
        reservationService.deleteWaitingByNameAndReservationId(name, reservationId);
    }

    public void updateMyReservation(UpdateMyReservation updateMyReservation, String name, Long reservationId) {
        ReservationResponse info = reservationService.findById(reservationId);
        Lock newSlotLock = getSlotLock(updateMyReservation.date(), updateMyReservation.timeId(), info.theme().id());
        newSlotLock.lock();
        try {
            Reservation reservation = reservationService.updateMyReservation(updateMyReservation, name, reservationId);
            try {
                reservationService.promoteFirstWaiting(reservation);
            } catch (Exception e) {
                reservationService.revertReservationUpdate(
                    reservationId,
                    reservation.getDate(),
                    reservation.getTime().getId(),
                    name
                );
                throw e;
            }
        } finally {
            newSlotLock.unlock();
        }
    }

    public List<MyReservationResponse> findReservationsByName(String name) {
        return reservationService.findAllByName(name);
    }

    @Transactional
    public void confirmPayment(ConfirmRequest request) {
        OrderRow order = orderService.findByOrderId(request.orderId());
        if (!order.amount().equals(request.amount())) {
            throw new PaymentAmountMismatchException("요청 금액과 인증 금액이 일치하지 않습니다.");
        }
        paymentService.approve(order.reservationId(), new PaymentConfirmation(request.paymentKey(),
            request.orderId(), request.amount()));
        reservationService.confirm(order.reservationId());
    }

    @Transactional
    public void cancelPendingByOrderId(String orderId) {
        Long reservationId = orderService.cancelByOrderId(orderId);
        reservationService.delete(reservationId);
    }

    @NonNull
    private static String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }

    private Lock getSlotLock(LocalDate date, Long timeId, Long themeId) {
        String key = date + ":" + timeId + ":" + themeId;
        return slotLocks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }
}
