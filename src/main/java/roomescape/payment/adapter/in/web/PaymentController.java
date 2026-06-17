package roomescape.payment.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.LoginMember;
import roomescape.payment.application.dto.request.PaymentConfirmRequest;
import roomescape.payment.application.dto.request.PaymentFailRequest;
import roomescape.payment.application.dto.response.PaymentConfigResponse;
import roomescape.payment.application.dto.response.PaymentConfirmResponse;
import roomescape.payment.application.port.in.ConfirmPaymentUseCase;
import roomescape.payment.application.port.in.HandlePaymentFailureUseCase;
import roomescape.payment.config.TossPaymentProperties;

@RestController
@RequestMapping("/api/user/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final HandlePaymentFailureUseCase handlePaymentFailureUseCase;
    private final TossPaymentProperties tossPaymentProperties;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<PaymentConfigResponse>> config() {
        return ResponseEntity.ok(ApiResponse.success(new PaymentConfigResponse(tossPaymentProperties.clientKey())));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirm(
            @RequestBody @Valid PaymentConfirmRequest request,
            @LoginMember AuthenticatedMember member
    ) {
        PaymentConfirmResponse response = confirmPaymentUseCase.confirm(request, member.id());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PostMapping("/fail")
    public ResponseEntity<ApiResponse<Void>> fail(
            @RequestBody PaymentFailRequest request,
            @LoginMember AuthenticatedMember member
    ) {
        handlePaymentFailureUseCase.handleFailure(request, member.id());
        return ResponseEntity.noContent().build();
    }
}
