package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.security.Accessor;
import roomescape.security.Auth;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public AdminController(ReservationService reservationService, ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addAdminReservation(
            @RequestBody @Valid AdminReservationRequest request
    ) {
        ReservationResponse reservationResponse = reservationService.addReservation(
                request.date(),
                request.timeId(),
                request.themeId(),
                request.memberId()
        );

        return ResponseEntity.created(URI.create("/reservation/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/waitings")
    public List<ReservationResponse> getReservations() {
        return reservationWaitingService.getReservationWaitings();
    }

    @DeleteMapping("/waitings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationWaiting(@PathVariable long id, @Auth Accessor accessor) {
        long memberId = accessor.id();
        reservationWaitingService.deleteReservationWaiting(id, memberId);
    }

}
