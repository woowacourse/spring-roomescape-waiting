package roomescape.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;
import roomescape.controller.dto.PaymentReadyResponse;
import roomescape.domain.Order;
import roomescape.service.OrderService;
import roomescape.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private static final long DEFAULT_AMOUNT = 50_000L;
    private static final String ORDER_NAME = "방탈출 예약";

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentController(
            OrderService orderService,
            PaymentService paymentService,
            @Value("${toss.client-key:}") String clientKey
    ) {
        this.orderService = orderService;
        this.paymentService = paymentService;
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
        paymentService.confirm(paymentKey, orderId, amount);
        return new RedirectView(
                "/payment-success.html?themeId=%d&orderId=%s&amount=%d".formatted(themeId, orderId, amount)
        );
    }

    @GetMapping("/fail")
    public RedirectView fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            @RequestParam Long themeId
    ) {
        log.info(
                "payments/fail reached. code={}, message={}, orderId={}, themeId={}",
                code,
                message,
                orderId,
                themeId
        );

        if (StringUtils.hasText(orderId)) {
            paymentService.fail(orderId);
        }

        String redirectUrl = "/payment-fail.html?themeId=%d&orderId=%s&paymentCode=%s&paymentMessage=%s"
                .formatted(themeId, defaultString(orderId), defaultString(code), encode(message));
        return new RedirectView(redirectUrl);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String encode(String value) {
        return URLEncoder.encode(defaultString(value), StandardCharsets.UTF_8);
    }
}
