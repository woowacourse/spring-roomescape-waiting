package roomescape.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.reservation.ReservationCreateService;

import java.net.URI;

@RestController
public class WaitingApiController {

    private final ReservationCreateService reservationCreateService;


    public WaitingApiController(ReservationCreateService reservationCreateService) {
        this.reservationCreateService = reservationCreateService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> addWaiting(@RequestBody @Valid ReservationSaveRequest request,
                                                          @AuthenticatedMember Member member) {
        Reservation newReservation = reservationCreateService.createReservation(request, member, ReservationStatus.WAITING);
        return ResponseEntity.created(URI.create("/waiting/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }
}
