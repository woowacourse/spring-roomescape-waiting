package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.common.dto.ResourcesResponse;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class WaitingReservationApiController {

    private final ReservationService reservationService;

    public WaitingReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations/wait")
    public ResponseEntity<ResourcesResponse<WaitingResponse>> findWaitingReservations() {
        List<WaitingResponse> reservations = reservationService.findWaitingReservations();
        ResourcesResponse<WaitingResponse> response = new ResourcesResponse<>(reservations);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservations/wait")
    public ResponseEntity<ReservationResponse> createMemberWaitingReservation(
            @Valid @RequestBody ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember
    ) {
        ReservationResponse reservationResponse = reservationService.save(
                reservationSaveRequest,
                loginMember,
                Status.WAIT
        );

        return ResponseEntity.created(URI.create("/reservations/wait/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @PatchMapping("/reservations/wait/{id}")
    public ResponseEntity<Void> updateSuccess(@PathVariable("id") Long id) {
        reservationService.updateSuccess(id);

        return ResponseEntity.noContent().build();
    }
}
