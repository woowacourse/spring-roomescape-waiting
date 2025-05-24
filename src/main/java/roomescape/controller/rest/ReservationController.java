package roomescape.controller.rest;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.dto.SessionMember;
import roomescape.service.ReservationService;
import roomescape.service.request.CreateReservationRequest;
import roomescape.service.response.MyReservationResponse;
import roomescape.service.response.ReservationResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid final CreateReservationRequest request,
            final SessionMember sessionMember
    ) {
        ReservationResponse response = reservationService.createReservation(request, sessionMember.id());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservation(
            final SessionMember sessionMember
    ) {
        final List<MyReservationResponse> response = reservationService.findAllMyReservation(sessionMember.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationsWithFilter(
            @RequestParam(required = false) final Long memberId,
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final LocalDate fromDate,
            @RequestParam(required = false) final LocalDate toDate
    ) {
        final List<ReservationResponse> responses = reservationService.findAllReservationsWithFilter(
                memberId,
                themeId,
                fromDate,
                toDate
        );
        return ResponseEntity.ok(responses);
    }
}
