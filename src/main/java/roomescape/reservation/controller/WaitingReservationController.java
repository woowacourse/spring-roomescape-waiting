package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.domain.AuthInfo;
import roomescape.global.annotation.LoginUser;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.MemberReservationService;

import java.net.URI;

@RestController
@RequestMapping("/reservations/waiting")
public class WaitingReservationController {

    private final MemberReservationService memberReservationService;

    public WaitingReservationController(MemberReservationService memberReservationService) {
        this.memberReservationService = memberReservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@LoginUser AuthInfo authInfo,
                                                      @RequestBody @Valid ReservationRequest reservationRequest) {
        ReservationResponse response = memberReservationService.createReservation(authInfo, reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.reservationId())).body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@LoginUser AuthInfo authInfo,
                       @PathVariable("id") @Min(1) long reservationMemberId) {
        memberReservationService.deleteMemberReservation(authInfo, reservationMemberId);
    }
}
