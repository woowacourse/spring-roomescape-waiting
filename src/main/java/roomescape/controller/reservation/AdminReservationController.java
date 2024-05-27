package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.reservation.dto.AdminCreateReservationRequest;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getReservations() {
        return reservationService.getReservedReservations();
    }

    @GetMapping(value = "/search", params = {"themeId", "memberId", "dateFrom", "dateTo"})
    public List<ReservationResponse> searchReservations(
            @Valid final ReservationSearchCondition request) {
        return reservationService.searchReservations(request);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid final AdminCreateReservationRequest adminRequest) {
        CreateReservationRequest reservationRequest = new CreateReservationRequest(
                adminRequest.date(), adminRequest.timeId(), adminRequest.themeId());
        final ReservationResponse reservation = reservationService
                .addReservedReservation(reservationRequest, adminRequest.memberId());

        final URI uri = UriComponentsBuilder.fromPath("/reservations/{id}")
                .buildAndExpand(reservation.id())
                .toUri();
        return ResponseEntity.created(uri).body(reservation);
    }
}
