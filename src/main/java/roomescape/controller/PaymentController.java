package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;
import roomescape.controller.dto.PaymentReadyResponse;
import roomescape.domain.Order;
import roomescape.service.OrderService;
import roomescape.service.ReservationService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private static final long DEFAULT_AMOUNT = 50_000L;
    private static final String ORDER_NAME = "방탈출 예약";

    private final OrderService orderService;
    private final ReservationService reservationService;
    private final String clientKey;

    public PaymentController(
            OrderService orderService,
            ReservationService reservationService,
            @Value("${toss.client-key:}") String clientKey
    ) {
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.clientKey = clientKey;
    }

    @ResponseBody
    @PostMapping("/ready")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentReadyResponse ready(@RequestParam Long reservationId) {
        Order order = orderService.create(DEFAULT_AMOUNT, reservationId);
        return PaymentReadyResponse.from(clientKey, order, ORDER_NAME);
    }

    @GetMapping("/success")
    public RedirectView success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            @RequestParam Long themeId
    ) {
        orderService.validateAmount(orderId, amount);
        reservationService.confirm(orderService.findReservationId(orderId));
        return redirectReservation(themeId, "success");
    }

    @GetMapping("/fail")
    public RedirectView fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            @RequestParam Long themeId
    ) {
        if (orderId != null) {
            reservationService.fail(orderService.findReservationId(orderId));
        }
        return redirectReservation(themeId, "fail");
    }

    private RedirectView redirectReservation(Long themeId, String paymentResult) {
        return new RedirectView("/reservation.html?themeId=%d&paymentResult=%s".formatted(themeId, paymentResult));
    }
}
