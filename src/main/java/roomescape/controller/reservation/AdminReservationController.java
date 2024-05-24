package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.reservation.dto.AdminCreateReservationRequest;
import roomescape.controller.reservation.dto.CreateReservationDto;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.domain.Status;
import roomescape.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid final AdminCreateReservationRequest adminRequest) {
        final CreateReservationDto request = new CreateReservationDto(
                adminRequest.memberId(), adminRequest.themeId(), adminRequest.date(),
                adminRequest.timeId(), Status.RESERVED);
        final ReservationResponse reservation = reservationService.addReservation(request);

        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(reservation.id())
                .toUri();
        return ResponseEntity.created(uri).body(reservation);
    }
}
