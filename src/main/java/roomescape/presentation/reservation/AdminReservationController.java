package roomescape.presentation.reservation;

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
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.WaitingService;
import roomescape.application.reservation.dto.request.ReservationFilterRequest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.presentation.auth.LoginMemberId;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid ReservationRequest request) {
        ReservationResponse response = reservationService.create(request);
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findByFilter(@RequestParam(required = false) Long memberId,
                                                                  @RequestParam(required = false) Long themeId,
                                                                  @RequestParam(required = false) LocalDate startDate,
                                                                  @RequestParam(required = false) LocalDate endDate) {
        ReservationFilterRequest request = new ReservationFilterRequest(memberId, themeId, startDate, endDate);
        List<ReservationResponse> responses = reservationService.findByFilter(request);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> findAllWaiting() {
        List<ReservationResponse> responses = waitingService.findAll();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@LoginMemberId long memberId, @PathVariable long id) {
        waitingService.deleteById(memberId, id);
        return ResponseEntity.noContent().build();
    }

}
