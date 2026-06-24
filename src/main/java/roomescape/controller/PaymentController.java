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
import roomescape.domain.PaymentOrder;
import roomescape.repository.PaymentOrderRepository;
import roomescape.service.PaymentService;
import roomescape.service.ReservationService;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final PaymentOrderRepository paymentOrderRepository;
    private final String clientKey;

    public PaymentController(
            PaymentService paymentService,
            ReservationService reservationService,
            PaymentOrderRepository paymentOrderRepository,
            @Value("${toss.client-key}") String clientKey) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.paymentOrderRepository = paymentOrderRepository;
        this.clientKey = clientKey;
    }

    @GetMapping
    public String paymentPage(@RequestParam String orderId, Model model) {
        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId)
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
        paymentService.confirm(paymentKey, orderId, amount);
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
