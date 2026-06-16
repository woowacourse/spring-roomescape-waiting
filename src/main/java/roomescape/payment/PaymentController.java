package roomescape.payment;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.exception.RoomescapeException;
import roomescape.payment.client.TossPaymentException;
import roomescape.payment.dto.CheckoutResult;
import roomescape.payment.dto.PaymentConfirmResult;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentController(PaymentService paymentService, @Value("${toss.client-key:}") String clientKey) {
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/payments/checkout")
    public String checkout(
            @RequestParam String name,
            @RequestParam LocalDate date,
            @RequestParam Long timeId,
            @RequestParam Long themeId,
            @RequestParam(defaultValue = "") String themeName,
            Model model
    ) {
        CheckoutResult result = paymentService.checkout(new ReservationRequest(name, date, timeId, themeId), themeName);
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", result.orderId());
        model.addAttribute("orderName", result.orderName());
        model.addAttribute("amount", result.amount());
        return "payment/checkout";
    }

    @GetMapping("/payments/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            PaymentConfirmResult result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("payment", result.tossResponse());
            model.addAttribute("reservation", result.reservation());
            return "payment/success";
        } catch (RoomescapeException e) {
            return failView(model, e.getErrorCode().name(), e.getMessage(), orderId);
        } catch (TossPaymentException e) {
            return failView(model, e.getCode(), e.getMessage(), orderId);
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }
}
