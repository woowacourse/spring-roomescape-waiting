package roomescape.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.domain.Order;
import roomescape.domain.PaymentStatus;
import roomescape.dto.response.PaymentResult;
import roomescape.repository.OrderRepository;
import roomescape.service.PaymentService;
import roomescape.service.ReservationService;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final OrderRepository orderRepository;
    private final String clientKey;

    public PaymentController(
            PaymentService paymentService,
            ReservationService reservationService,
            OrderRepository orderRepository,
            @Value("${toss.client-key}") String clientKey) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.orderRepository = orderRepository;
        this.clientKey = clientKey;
    }

    @GetMapping
    public String paymentPage(@RequestParam String orderId, Model model) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.NOT_FOUND));
        String orderName = order.getReservation().getTheme().getName() + " 방탈출 예약";

        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", order.getAmount());
        return "payment";
    }

    @GetMapping("/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount) {
        PaymentResult paymentResult = paymentService.confirm(paymentKey, orderId, amount);
        if (paymentResult.status() == PaymentStatus.NO_RESPONSE) {
            return "redirect:/my-reservations.html?payment=pending";
        }
        return "redirect:/my-reservations.html?payment=success";
    }

    @GetMapping("/fail")
    public String fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId) {
        reservationService.cancelPendingByOrderId(orderId);
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "redirect:/reservation.html?error=" + encodedMessage;
    }
}
