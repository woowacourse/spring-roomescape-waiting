package roomescape.member.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.dto.MemberReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final ReservationService reservationService;
    private final MemberService memberService;

    public AdminController(ReservationService reservationService, MemberService memberService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid MemberReservationRequest memberReservationRequest) {
        ReservationResponse reservationResponse = reservationService.createMemberReservation(memberReservationRequest);
        return ResponseEntity.created(URI.create("/admin/reservations/" + reservationResponse.memberReservationId()))
                .body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") @Min(1) long reservationId) {
        reservationService.delete(reservationId);
    }

    @GetMapping("/members")
    public List<MemberResponse> findAll() {
        return memberService.findAll();
    }
}
