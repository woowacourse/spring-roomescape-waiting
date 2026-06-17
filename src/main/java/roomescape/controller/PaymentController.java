package roomescape.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ControllerPaymentConfirmRequest;
import roomescape.controller.dto.request.ControllerPaymentFailRequest;
import roomescape.controller.dto.response.PaymentConfigResponse;
import roomescape.controller.dto.response.PaymentHistoryResponse;
import roomescape.controller.dto.response.ReceptionResponse;
import roomescape.facade.ReceptionFacade;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final ReceptionFacade receptionFacade;
    private final ReservationService reservationService;
    private final String clientKey;

    public PaymentController(ReceptionFacade receptionFacade,
                             ReservationService reservationService,
                             @Value("${toss.client-key}") String clientKey) {
        this.receptionFacade = receptionFacade;
        this.reservationService = reservationService;
        this.clientKey = clientKey;
    }

    @GetMapping(params = "name")
    public ResponseEntity<List<PaymentHistoryResponse>> history(@RequestParam("name") String name) {
        return ResponseEntity.ok(reservationService.findPaymentHistoryByName(name).stream()
                .map(PaymentHistoryResponse::from)
                .toList());
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> config() {
        return ResponseEntity.ok(new PaymentConfigResponse(clientKey));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ReceptionResponse> confirm(@RequestBody ControllerPaymentConfirmRequest request) {
        return ResponseEntity.ok(receptionFacade.confirmPayment(request.paymentKey(), request.orderId(),
                request.amount()));
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(@RequestBody ControllerPaymentFailRequest request) {
        receptionFacade.failPayment(request.orderId());
        return ResponseEntity.noContent().build();
    }
}
