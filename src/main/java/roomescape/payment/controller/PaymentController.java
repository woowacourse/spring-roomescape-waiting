package roomescape.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.dto.request.ConfirmRequest;
import roomescape.reservation.service.ReservationFacade;

@RestController
@RequestMapping("/payments")
public class PaymentController {

  private final ReservationFacade reservationFacade;

  public PaymentController(ReservationFacade reservationFacade) {
    this.reservationFacade = reservationFacade;
  }

  @PostMapping("/confirm")
  public ResponseEntity<Void> confirm(@RequestBody ConfirmRequest confirmRequest) {
    reservationFacade.confirmPayment(confirmRequest);
    return ResponseEntity.ok().build();
  }
}
