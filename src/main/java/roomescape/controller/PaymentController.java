package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.dto.PaymentFailRequest;
import roomescape.service.PaymentService;

@RequestMapping("/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/confirm")
    public void confirm(@Valid @RequestBody PaymentConfirmRequest request) {

        paymentService.confirm(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/fail")
    public void fail(@RequestBody PaymentFailRequest request) {
        paymentService.fail(request);
    }
}
