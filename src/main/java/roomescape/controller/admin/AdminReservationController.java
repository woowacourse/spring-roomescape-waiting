package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.WaitingReservationResponse;
import roomescape.service.reservation.ReservationService;

@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid final CreateReservationRequest request) {
        ReservationResponse response = reservationService.addReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationsByFilter(
            @RequestParam(required = false, name = "memberId") Long memberId,
            @RequestParam(required = false, name = "themeId") Long themeId,
            @RequestParam(required = false, name = "dateFrom") LocalDate dateFrom,
            @RequestParam(required = false, name = "dateTo") LocalDate dateTo
    ) {
        return ResponseEntity.status(HttpStatus.OK).body( reservationService.getFilteredReservations(memberId, themeId, dateFrom, dateTo));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<WaitingReservationResponse>> getWaitingReservations() {
        List<WaitingReservationResponse> waitingReservations = reservationService.getAllWaitingReservations();
        return ResponseEntity.ok(waitingReservations);
    }

    @DeleteMapping("/{reservationId}/pending/deny")
    public ResponseEntity<Void> denyWaitingReservation(@PathVariable Long reservationId) {
        reservationService.denyPendingReservation(reservationId);
        return ResponseEntity.ok().build();
    }
}
