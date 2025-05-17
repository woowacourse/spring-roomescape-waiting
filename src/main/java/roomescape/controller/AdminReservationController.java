package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.request.CreateReservationAdminRequest;
import roomescape.controller.response.ReservationResponse;
import roomescape.service.ReservationCreationService;
import roomescape.service.ReservationService;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationCreationService reservationCreationService;
    private final ReservationService reservationService;

    public AdminReservationController(ReservationCreationService reservationCreationService, ReservationService reservationService) {
        this.reservationCreationService = reservationCreationService;
        this.reservationService = reservationService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ReservationResponse createReservation(@RequestBody CreateReservationAdminRequest reservationRequest) {
        CreateReservationParam createReservationParam = new CreateReservationParam(
                reservationRequest.memberId(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );
        ReservationResult reservationResult = reservationCreationService.create(createReservationParam);
        return ReservationResponse.from(reservationResult);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId) {
        reservationService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }
}
