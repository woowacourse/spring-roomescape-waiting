package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.controller.api.dto.request.ReservationRequest;
import roomescape.controller.api.dto.response.MemberReservationsResponse;
import roomescape.controller.api.dto.response.ReservationResponse;
import roomescape.controller.api.dto.response.ReservationsResponse;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.output.ReservationOutput;
import roomescape.service.dto.output.WaitingOutput;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationApiController(final ReservationService reservationService, final WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody final ReservationRequest reservationRequest,
                                                                 final LoginMemberRequest loginMemberRequest) {
        final ReservationOutput output = reservationService.createReservation(reservationRequest.toInput(loginMemberRequest.id()));
        return ResponseEntity.created(URI.create("/reservations/" + output.id()))
                .body(ReservationResponse.toResponse(output));
    }

    @GetMapping
    public ResponseEntity<ReservationsResponse> getAllReservations() {
        final List<ReservationOutput> outputs = reservationService.getAllReservations();
        return ResponseEntity.ok(ReservationsResponse.toResponse(outputs));
    }

    @GetMapping("/search")
    public ResponseEntity<ReservationsResponse> searchReservation(
            @RequestParam final long themeId,
            @RequestParam final long memberId,
            @RequestParam final LocalDate fromDate,
            @RequestParam final LocalDate toDate) {
        final List<ReservationOutput> outputs = reservationService.searchReservation(new ReservationSearchInput(themeId, memberId, fromDate, toDate));
        return ResponseEntity.ok(ReservationsResponse.toResponse(outputs));
    }

    @GetMapping("/mine")
    public ResponseEntity<MemberReservationsResponse> getMyReservations(final LoginMemberRequest loginMemberRequest) {
        final List<ReservationOutput> outputs = reservationService.getAllMyReservations(loginMemberRequest.id());
        final List<WaitingOutput> waitingOutputs = waitingService.getAllMyWaiting(loginMemberRequest.id());
        final var result = MemberReservationsResponse.toResponse(outputs,waitingOutputs);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable final long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent()
                .build();
    }
}
