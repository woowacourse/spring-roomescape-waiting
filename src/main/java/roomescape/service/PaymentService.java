package roomescape.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.controller.dto.payment.PaymentConfirmResponse;
import roomescape.controller.dto.payment.PaymentFailResponse;
import roomescape.controller.dto.payment.PaymentOrderRequest;
import roomescape.controller.dto.payment.PaymentOrderResponse;
import roomescape.domain.Member;
import roomescape.domain.Schedule;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentCheckoutProperties;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentOrder;
import roomescape.payment.PaymentOrderIdGenerator;
import roomescape.payment.PaymentOrderStatus;
import roomescape.payment.PaymentResult;
import roomescape.repository.PaymentOrderDao;
import roomescape.repository.MemberDao;
import roomescape.repository.ReservationDao;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private static final int EMPTY_RESERVATION_COUNT = 0;

    private final PaymentOrderDao paymentOrderDao;
    private final MemberDao memberDao;
    private final ReservationDao reservationDao;
    private final ScheduleService scheduleService;
    private final ReservationService reservationService;
    private final PaymentGateway paymentGateway;
    private final PaymentOrderIdGenerator orderIdGenerator;
    private final PaymentCheckoutProperties checkoutProperties;

    public PaymentService(
            PaymentOrderDao paymentOrderDao,
            MemberDao memberDao,
            ReservationDao reservationDao,
            ScheduleService scheduleService,
            ReservationService reservationService,
            PaymentGateway paymentGateway,
            PaymentOrderIdGenerator orderIdGenerator,
            PaymentCheckoutProperties checkoutProperties
    ) {
        this.paymentOrderDao = paymentOrderDao;
        this.memberDao = memberDao;
        this.reservationDao = reservationDao;
        this.scheduleService = scheduleService;
        this.reservationService = reservationService;
        this.paymentGateway = paymentGateway;
        this.orderIdGenerator = orderIdGenerator;
        this.checkoutProperties = checkoutProperties;
    }

    @Transactional
    public PaymentOrderResponse createOrder(PaymentOrderRequest request, Member member, String baseUrl) {
        validateCheckoutConfigured();
        Schedule schedule = scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId());
        validatePaymentStartable(member, schedule);

        LocalDateTime now = LocalDateTime.now();
        String orderId = orderIdGenerator.generate();
        int amount = schedule.getTheme().getPrice();
        PaymentOrder order = new PaymentOrder(
                null,
                orderId,
                member.getId(),
                schedule.getId(),
                amount,
                PaymentOrderStatus.PENDING,
                null,
                null,
                null,
                null,
                now,
                now
        );
        paymentOrderDao.save(order);

        return new PaymentOrderResponse(
                checkoutProperties.getClientKey(),
                orderId,
                schedule.getTheme().getName(),
                amount,
                baseUrl + "/payments/success",
                baseUrl + "/payments/fail"
        );
    }

    @Transactional
    public PaymentConfirmResponse confirm(String paymentKey, String orderId, int callbackAmount) {
        PaymentOrder order = getOrder(orderId);

        if (order.isConfirmedWith(paymentKey)) {
            return new PaymentConfirmResponse(
                    order.getOrderId(),
                    order.getPaymentKey(),
                    order.getReservationId(),
                    order.getAmount(),
                    order.getStatus().name()
            );
        }

        validateAmount(order, callbackAmount);
        validateSlotStillAvailable(order);

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, order.getAmount()));
        Long reservationId = reservationService.saveConfirmedReservationByPayment(order.getScheduleId(), getMember(order));
        paymentOrderDao.confirm(orderId, result.paymentKey(), reservationId, LocalDateTime.now());

        return new PaymentConfirmResponse(
                result.orderId(),
                result.paymentKey(),
                reservationId,
                result.totalAmount(),
                PaymentOrderStatus.CONFIRMED.name()
        );
    }

    @Transactional
    public PaymentFailResponse fail(String code, String message, String orderId) {
        if (orderId != null && !orderId.isBlank()) {
            paymentOrderDao.fail(orderId, code, message, LocalDateTime.now());
        }
        return new PaymentFailResponse(code, message, orderId);
    }

    private void validatePaymentStartable(Member member, Schedule schedule) {
        validateNotPastSchedule(schedule);
        if (reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 해당 스케줄에 본인의 예약이 존재합니다.");
        }
        if (reservationDao.countReservationByScheduleId(schedule.getId()) != EMPTY_RESERVATION_COUNT) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE, "이미 예약된 슬롯은 결제할 수 없습니다. 예약 대기를 이용해주세요.");
        }
    }

    private void validateNotPastSchedule(Schedule schedule) {
        LocalDateTime reservationDateTime = LocalDateTime.of(schedule.getDate(), schedule.getTime().getStartAt());
        if (!reservationDateTime.isAfter(LocalDateTime.now())) {
            throw new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "과거 시각으로는 결제를 시작할 수 없습니다.");
        }
    }

    private void validateCheckoutConfigured() {
        if (!checkoutProperties.isConfigured()) {
            throw new RoomescapeException(
                    DomainErrorCode.PAYMENT_AUTHENTICATION_FAILED,
                    "Toss 표준 결제창 클라이언트 키(test_ck_)가 설정되지 않았습니다. " + checkoutProperties.diagnostics()
            );
        }
    }

    private void validateSlotStillAvailable(PaymentOrder order) {
        scheduleService.lockById(order.getScheduleId());
        if (reservationDao.countReservationByScheduleId(order.getScheduleId()) != EMPTY_RESERVATION_COUNT) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE, "이미 예약된 슬롯은 결제 승인할 수 없습니다.");
        }
    }

    private void validateAmount(PaymentOrder order, int callbackAmount) {
        if (order.getAmount() != callbackAmount) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH, "결제 금액이 주문 금액과 일치하지 않습니다.");
        }
    }

    private PaymentOrder getOrder(String orderId) {
        return paymentOrderDao.findByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER, "주문 정보를 찾을 수 없습니다. orderId: " + orderId));
    }

    private Member getMember(PaymentOrder order) {
        return memberDao.findById(order.getMemberId())
                .orElseThrow(() -> new RoomescapeException(DomainErrorCode.INVALID_INPUT, "주문 회원을 찾을 수 없습니다. ID: " + order.getMemberId()));
    }
}
