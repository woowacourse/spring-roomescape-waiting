package roomescape.presentation.api;

import jakarta.validation.Valid;
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
import roomescape.application.ReservationService;
import roomescape.application.WaitingService;
import roomescape.presentation.dto.request.AdminReservationCreateRequest;
import roomescape.presentation.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminReservationController(
            ReservationService reservationService,
            WaitingService waitingService
    ) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody @Valid AdminReservationCreateRequest request
    ) {
        ReservationResponse response = reservationService.createAdminReservation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationsByFilter(
            @RequestParam Long themeId,
            @RequestParam Long memberId,
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {
        List<ReservationResponse> responses = reservationService.getReservationsByFilter(themeId, memberId, dateFrom, dateTo);

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaitingById(id);

        return ResponseEntity.noContent().build();
    }
}
