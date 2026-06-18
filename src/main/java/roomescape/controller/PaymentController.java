package roomescape.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import roomescape.controller.dto.payment.PaymentFailResponse;
import roomescape.controller.dto.payment.PaymentOrderHistoryResponse;
import roomescape.controller.dto.payment.PaymentOrderRequest;
import roomescape.controller.dto.payment.PaymentOrderResponse;
import roomescape.domain.Member;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.global.auth.LoginMember;
import roomescape.payment.PaymentOrder;
import roomescape.service.PaymentService;

@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @Valid @RequestBody PaymentOrderRequest request,
            @LoginMember Member member,
            HttpServletRequest servletRequest
    ) {
        String baseUrl = baseUrl(servletRequest);
        PaymentOrder order = paymentService.createOrder(request, member);
        return ResponseEntity.ok(PaymentOrderResponse.from(
                order,
                paymentService.getClientKey(),
                "방탈출 예약",
                baseUrl + "/payments/success",
                baseUrl + "/payments/fail"
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<PaymentOrderHistoryResponse>> findOrders(@LoginMember Member member) {
        List<PaymentOrderHistoryResponse> responses = paymentService.findOrdersByMember(member).stream()
                .map(PaymentOrderHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/success")
    public ResponseEntity<Void> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount
    ) {
        try {
            paymentService.confirm(paymentKey, orderId, amount);
        } catch (RoomescapeException e) {
            if (e.getCode() == DomainErrorCode.PAYMENT_CONFIRM_UNKNOWN) {
                return ResponseEntity.status(303)
                        .location(java.net.URI.create("/user.html?payment=unknown&message=" + encode(e.getMessage())))
                        .build();
            }
            throw e;
        }
        return ResponseEntity.status(303)
                .location(java.net.URI.create("/user.html?payment=success"))
                .build();
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        paymentService.fail(code, message, orderId);
        PaymentFailResponse response = new PaymentFailResponse(code, message, orderId);
        String redirect = "/user.html?payment=fail"
                + "&code=" + encode(response.code())
                + "&message=" + encode(response.message());
        return ResponseEntity.status(303)
                .location(java.net.URI.create(redirect))
                .build();
    }

    private String baseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private String encode(String text) {
        if (text == null) {
            return "";
        }
        return UriUtils.encode(text, StandardCharsets.UTF_8);
    }
}
