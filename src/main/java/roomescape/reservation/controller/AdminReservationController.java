package roomescape.reservation.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.controller.dto.MemberReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.MemberReservationService;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingReservationService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final MemberReservationService memberReservationService;
    private final WaitingReservationService waitingReservationService;

    public AdminReservationController(ReservationService reservationService, MemberReservationService memberReservationService, WaitingReservationService waitingReservationService) {
        this.reservationService = reservationService;
        this.memberReservationService = memberReservationService;
        this.waitingReservationService = waitingReservationService;
    }

    @PostMapping()
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid MemberReservationRequest memberReservationRequest) {
        ReservationResponse reservationResponse = memberReservationService.createMemberReservation(memberReservationRequest);
        return ResponseEntity.created(URI.create("/admin/reservations/" + reservationResponse.memberReservationId()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") @Min(1) long reservationId) {
        reservationService.delete(reservationId);
    }

    @GetMapping("/waiting")
    public List<ReservationResponse> waiting() {
        return waitingReservationService.findAllByWaitingReservation();
    }
}
