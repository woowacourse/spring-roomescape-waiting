package roomescape.payment.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.web.dto.PaymentCheckoutRequest;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.domain.exception.PaymentConfirmationPendingException;
import roomescape.payment.service.PaymentReadyOrder;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.Reservation;

@Controller
@RequestMapping("/payments")
public class PaymentPageController {
    private final PaymentService paymentService;
    private final PaymentProperties paymentProperties;

    public PaymentPageController(PaymentService paymentService, PaymentProperties paymentProperties) {
        this.paymentService = paymentService;
        this.paymentProperties = paymentProperties;
    }

    @PostMapping("/checkout")
    public String checkout(
            @Valid @ModelAttribute PaymentCheckoutRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return failView(model, "INVALID_CHECKOUT_REQUEST", firstErrorMessage(bindingResult), null);
        }

        try {
            PaymentReadyOrder paymentReadyOrder = paymentService.prepare(
                    request.name(),
                    request.date(),
                    request.timeId(),
                    request.themeId()
            );

            model.addAttribute("clientKey", paymentProperties.toss().clientKey());
            model.addAttribute("orderId", paymentReadyOrder.orderId());
            model.addAttribute("orderName", paymentReadyOrder.orderName());
            model.addAttribute("amount", paymentReadyOrder.amount());
            model.addAttribute("customerName", paymentReadyOrder.customerName());
            return "checkout";
        } catch (RuntimeException exception) {
            return failView(model, "CHECKOUT_PREPARE_FAILED", exception.getMessage(), null);
        }
    }

    @GetMapping("/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            Reservation reservation = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("reservation", ReservationResponse.from(reservation));
            model.addAttribute("paymentKey", paymentKey);
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount", amount);
            return "success";
        } catch (PaymentAmountMismatchException exception) {
            return failView(model, "AMOUNT_MISMATCH", exception.getMessage(), orderId);
        } catch (PaymentConfirmationPendingException exception) {
            return failView(
                    model,
                    PaymentConfirmationPendingException.CODE,
                    exception.getMessage(),
                    orderId,
                    "결제 결과 확인 필요"
            );
        } catch (RuntimeException exception) {
            return failView(model, "PAYMENT_CONFIRM_FAILED", exception.getMessage(), orderId);
        }
    }

    @GetMapping("/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        paymentService.fail(orderId, code, message);
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        return failView(model, code, message, orderId, "결제를 실패했어요");
    }

    private String failView(Model model, String code, String message, String orderId, String heading) {
        model.addAttribute("code", fallback(code, "PAYMENT_FAILED"));
        model.addAttribute("message", fallback(message, "결제가 완료되지 않았습니다."));
        model.addAttribute("orderId", orderId);
        model.addAttribute("heading", fallback(heading, "결제를 실패했어요"));
        return "fail";
    }

    private String firstErrorMessage(BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            return "요청 값이 올바르지 않습니다.";
        }
        return bindingResult.getAllErrors().getFirst().getDefaultMessage();
    }

    private String fallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
