package roomescape.payment;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.client.TossPaymentException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.service.ReservationService;

@Controller
@RequestMapping("/payments")
public class PaymentCallbackController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    public PaymentCallbackController(PaymentService paymentService, ReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @GetMapping("/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("result", result);
            return "payment/success";
        } catch (PaymentAmountMismatchException e) {
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
        } catch (TossPaymentException e) {
            return failView(model, e.getCode(), e.getMessage(), orderId);
        } catch (ReservationNotFoundException e) {
            return failView(model, "NOT_FOUND_RESERVATION", e.getMessage(), orderId);
        }
    }

    @GetMapping("/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        if (orderId != null) {
            reservationService.cancelByOrderId(orderId);
        }
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }
}
