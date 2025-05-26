package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.dto.response.AdminWaitingResponse;
import roomescape.entity.ConfirmedReservation;
import roomescape.entity.Reservation;
import roomescape.dto.request.AdminCreateReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.entity.WaitingReservation;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByFilter(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ConfirmedReservation> reservations = reservationService.findAllConfirmReservationByFilter(memberId, themeId, dateFrom,
                dateTo);
        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservationByAdmin(
            @RequestBody @Valid AdminCreateReservationRequest request
    ) {
        Reservation reservation = reservationService.addConfirmReservationByAdmin(request);
        ReservationResponse response = ReservationResponse.from(reservation);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/reservations/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<AdminWaitingResponse>> getWaitingResponse() {
        List<WaitingReservation> waitingReservations = reservationService.findAllWaitingReservation();
        List<AdminWaitingResponse> adminWaitingResponses = waitingReservations.stream()
                .map(AdminWaitingResponse::from)
                .toList();
        return ResponseEntity.ok(adminWaitingResponses);
    }
}
