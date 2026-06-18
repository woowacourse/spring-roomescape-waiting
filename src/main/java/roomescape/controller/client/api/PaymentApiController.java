package roomescape.controller.client.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.facade.OrderPayFacade;
import roomescape.application.service.result.PaymentApprovalResult;
import roomescape.controller.client.api.dto.request.PaymentConfirmRequest;
import roomescape.controller.client.api.dto.response.PaymentConfirmResponse;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final OrderPayFacade orderPayFacade;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request) {
        PaymentApprovalResult result = orderPayFacade.confirm(request.toCommand());
        return ResponseEntity.ok(PaymentConfirmResponse.from(
                result.payment(),
                result.orderType(),
                result.targetId()
        ));
    }
}
