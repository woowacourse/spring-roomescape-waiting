package roomescape.member.ui.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.reservation.dto.ReservationInfo;
import roomescape.reservation.application.reservation.service.ReservationService;
import roomescape.reservation.application.waiting.service.ReservationWaitingService;
import roomescape.reservation.ui.reservation.dto.AdminReservationCreateRequest;
import roomescape.reservation.ui.reservation.dto.ReservationResponse;
import roomescape.reservation.ui.waiting.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/admin")
public class AdminApiController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public AdminApiController(final ReservationService reservationService, ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid final AdminReservationCreateRequest request) {
        final ReservationInfo reservationInfo = reservationService.createReservation(request.convertToCreateCommand());
        final URI uri = URI.create("/reservations/" + reservationInfo.id());
        final ReservationResponse response = new ReservationResponse(reservationInfo);
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<ReservationWaitingResponse>> getWaitings() {
        final List<ReservationWaitingResponse> responses = reservationWaitingService.findAll()
                .stream()
                .map(ReservationWaitingResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationWaitingService.cancelReservationWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
