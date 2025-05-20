package roomescape.presentation.api.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.command.CreateReservationService;
import roomescape.application.reservation.query.ReservationQueryService;
import roomescape.application.reservation.query.dto.ReservationResult;
import roomescape.presentation.api.reservation.request.CreateReservationRequest;
import roomescape.presentation.api.reservation.response.ReservationResponse;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final String RESERVATIONS_URL = "/reservations/%d";

    private final CreateReservationService createReservationService;
    private final ReservationQueryService reservationQueryService;

    public ReservationController(CreateReservationService createReservationService,
                                 ReservationQueryService reservationQueryService) {
        this.createReservationService = createReservationService;
        this.reservationQueryService = reservationQueryService;
    }

    @PostMapping
    public ResponseEntity<Void> createReservation(
            @AuthPrincipal AuthInfo authInfo,
            @Valid @RequestBody CreateReservationRequest createReservationRequest) {
        Long id = createReservationService.reserve(createReservationRequest.toCreateCommand(authInfo.memberId()));
        return ResponseEntity.created(URI.create(RESERVATIONS_URL.formatted(id)))
                .build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        List<ReservationResult> reservationResults = reservationQueryService.findAll();
        List<ReservationResponse> reservationResponses = reservationResults.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservationResponses);
    }
}
