package roomescape.presentation.api.reservation;

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
import roomescape.application.reservation.command.CreateReservationService;
import roomescape.application.reservation.command.DeleteReservationService;
import roomescape.application.reservation.query.ReservationQueryService;
import roomescape.application.reservation.query.dto.ReservationResult;
import roomescape.application.reservation.query.dto.ReservationSearchCondition;
import roomescape.presentation.api.reservation.request.CreateAdminReservationRequest;
import roomescape.presentation.api.reservation.response.ReservationResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private static final String RESERVATIONS_URL = "/reservations/%d";

    private final CreateReservationService createReservationService;
    private final DeleteReservationService deleteReservationService;
    private final ReservationQueryService reservationQueryService;

    public AdminReservationController(CreateReservationService createReservationService,
                                      DeleteReservationService deleteReservationService,
                                      ReservationQueryService reservationQueryService) {
        this.createReservationService = createReservationService;
        this.deleteReservationService = deleteReservationService;
        this.reservationQueryService = reservationQueryService;
    }

    @PostMapping
    public ResponseEntity<Void> createReservation(
            @Valid @RequestBody CreateAdminReservationRequest createAdminReservationRequest) {
        Long id = createReservationService.reserve(createAdminReservationRequest.toCreateCommand());
        return ResponseEntity.created(URI.create(RESERVATIONS_URL.formatted(id)))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long reservationId) {
        deleteReservationService.removeById(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(@RequestParam("themeId") Long themeId,
                                                             @RequestParam("memberId") Long memberId,
                                                             @RequestParam("from") LocalDate from,
                                                             @RequestParam("to") LocalDate to) {
        List<ReservationResult> reservationResults = reservationQueryService.findReservationsBy(
                new ReservationSearchCondition(themeId, memberId, from, to)
        );
        List<ReservationResponse> reservationResponses = reservationResults.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservationResponses);
    }
}
