package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import roomescape.controller.view.dto.PaymentFailRequest;
import roomescape.controller.view.dto.PaymentReservationView;
import roomescape.controller.view.dto.PaymentSuccessRequest;
import roomescape.domain.Reservation;
import roomescape.service.payment.PaymentAmountMismatchException;
import roomescape.service.payment.PaymentGatewayException;
import roomescape.service.payment.PaymentResult;
import roomescape.service.PaymentService;

@Controller
public class PaymentSuccessController {

    private final PaymentService paymentService;

    public PaymentSuccessController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/success")
    public String success(
            @ModelAttribute PaymentSuccessRequest request,
            Model model
    ) {
        try {
            PaymentResult result = paymentService.confirm(request.paymentKey(), request.orderId(), request.amount());
            model.addAttribute("result", result);
            addReservationViewByOrderId(model, request.orderId());
            model.addAttribute("name", request.name());
            return "payment-success";
        } catch (PaymentAmountMismatchException e) {
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), request.orderId(), request.name(),
                    paymentService.findReservationByOrderId(request.orderId()));
        } catch (PaymentGatewayException e) {
            return failView(model, e.getCode(), e.getMessage(), request.orderId(), request.name(),
                    paymentService.findReservationByOrderId(request.orderId()));
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @ModelAttribute PaymentFailRequest request,
            Model model
    ) {
        Reservation reservation = null;
        if (request.paymentId() != null) {
            paymentService.fail(request.paymentId(), request.code(), request.message());
            reservation = paymentService.findReservationByPaymentId(request.paymentId());
        } else if (request.orderId() != null) {
            reservation = paymentService.findReservationByOrderId(request.orderId());
        }
        return failView(model, request.code(), request.message(), request.orderId(), request.name(), reservation);
    }

    private void addReservationViewByOrderId(Model model, String orderId) {
        Reservation reservation = paymentService.findReservationByOrderId(orderId);
        model.addAttribute("reservation", PaymentReservationView.from(reservation));
    }

    private String failView(Model model, String code, String message, String orderId, String name,
                            Reservation reservation) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        model.addAttribute("name", name);
        if (reservation != null) {
            model.addAttribute("reservation", PaymentReservationView.from(reservation));
        }
        return "payment-fail";
    }
}
