package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.PaymentService;
import roomescape.service.dto.command.PaymentCreateCommand;
import roomescape.service.dto.command.PaymentSuccessCommand;
import roomescape.service.dto.result.PaymentConfirmResult;
import roomescape.service.dto.result.PaymentReadyResult;

import java.net.URI;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentReadyResult> create(
            @Valid @RequestBody final PaymentCreateCommand request
    ) {
        final PaymentReadyResult result = paymentService.create(request);
        return ResponseEntity.created(URI.create("/payments/" + result.id()))
                .body(result);
    }

    @PostMapping("/success")
    public ResponseEntity<PaymentConfirmResult> confirm(
            @Valid @RequestBody final PaymentSuccessCommand request
    ) {
        final PaymentConfirmResult result = paymentService.confirm(request);
        return ResponseEntity.ok()
                .body(result);
    }

    @DeleteMapping("/{order-id}")
    public ResponseEntity<Void> fail(
            @PathVariable("order-id") final String orderId
    ) {
        paymentService.fail(orderId);
        return ResponseEntity.ok().build();
    }
}
