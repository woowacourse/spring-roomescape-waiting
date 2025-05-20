package roomescape.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ReservationRequestV2;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationService reservationServiceV2;

    public ReservationController(final ReservationService reservationService,
                                 final ReservationService reservationServiceV2) {
        this.reservationService = reservationService;
        this.reservationServiceV2 = reservationServiceV2;
    }

    @GetMapping()
    public ResponseEntity<List<ReservationResponse>> reservationList() {
        return ResponseEntity.status(HttpStatus.OK).body(reservationServiceV2.getAllReservations());
    }

    @PostMapping()
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid final ReservationRequestV2 request, final Long memberId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationServiceV2.addReservationWithMemberId(request, memberId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeReservation(@PathVariable(name = "id") long id) {
        reservationService.removeReservation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
