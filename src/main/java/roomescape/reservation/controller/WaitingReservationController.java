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
import roomescape.reservation.dto.request.ReservationDetailRequest;
import roomescape.reservation.dto.request.WaitingReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.service.WaitingReservationService;

@RestController
public class WaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    public WaitingReservationController(WaitingReservationService waitingReservationService) {
        this.waitingReservationService = waitingReservationService;
    }

    @GetMapping("/reservations/wait")
    public ResponseEntity<ResourcesResponse<WaitingResponse>> findAll() {
        List<WaitingResponse> reservations = waitingReservationService.findAll();
        ResourcesResponse<WaitingResponse> response = new ResourcesResponse<>(reservations);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservations/wait")
    public ResponseEntity<ReservationResponse> save(
            @Valid @RequestBody ReservationDetailRequest reservationDetail,
            LoginMember loginMember
    ) {
        WaitingReservationSaveRequest request = WaitingReservationSaveRequest.of(reservationDetail, loginMember.id());
        ReservationResponse response = waitingReservationService.save(request);

        return ResponseEntity.created(URI.create("/reservations/wait/" + response.id()))
                .body(response);
    }

    @PatchMapping("/reservations/wait/{id}")
    public ResponseEntity<Void> approveReservation(@PathVariable("id") Long id) {
        waitingReservationService.approveReservation(id);

        return ResponseEntity.noContent().build();
    }
}
