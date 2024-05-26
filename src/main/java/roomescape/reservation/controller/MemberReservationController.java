package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.annotation.AuthenticationPrincipal;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.MemberReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.SelectableTimeResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @RequestBody @Valid final ReservationSaveRequest reservationSaveRequest,
            @AuthenticationPrincipal Member member
    ) {
        ReservationResponse reservationResponse = reservationService.reserve(reservationSaveRequest, member);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> registerWaiting(
            @RequestBody @Valid final ReservationSaveRequest reservationSaveRequest,
            @AuthenticationPrincipal Member member
    ) {
        ReservationResponse reservationResponse = reservationService.registerWaiting(reservationSaveRequest, member);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        return ResponseEntity.ok(reservationService.getAllResponses());
    }

    @GetMapping("/times")
    public ResponseEntity<List<SelectableTimeResponse>> findSelectableTimes(
            @RequestParam(name = "date") final LocalDate date,
            @RequestParam(name = "themeId") final long themeId
    ) {
        return ResponseEntity.ok(reservationService.findSelectableTimes(date, themeId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MemberReservationResponse>> findMemberReservations(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(reservationService.findAllByMemberId(member.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationDeleteResponse> delete(@PathVariable("id") final long id) {
        return ResponseEntity.ok().body(reservationService.delete(id));
    }
}
