package roomescape.service;

import org.springframework.stereotype.Service;

import roomescape.domain.Order;
import roomescape.domain.repository.OrderRepository;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class PaymentService {
    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;

    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void confirm(PaymentConfirmRequest request) {
        Order order = orderRepository.getById(request.orderId());
        if(!order.getAmount().equals(request.amount())){
            log.warn("Payment amount mismatch. expected={}, actual={}", order.getAmount(), request.amount());
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}
