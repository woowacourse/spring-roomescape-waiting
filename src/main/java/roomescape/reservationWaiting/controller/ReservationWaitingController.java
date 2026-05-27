package roomescape.reservationWaiting.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.exception.MissingAuthorizationHeaderException;
import roomescape.reservationWaiting.controller.dto.ReservationWaitingRequest;
import roomescape.reservationWaiting.controller.dto.ReservationWaitingResponse;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.service.ReservationWaitingService;

@RestController
@RequestMapping("/reservations-waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @RequestBody ReservationWaitingRequest request) {
        ReservationWaiting reservationWaiting = reservationWaitingService.makeReservationWaiting(request.toCommand());
        ReservationWaitingResponse response = ReservationWaitingResponse.from(reservationWaiting);

        return ResponseEntity
                .created(URI.create("/reservations-waitings/" + response.id()))
                .body(response);
    }

    //- DELETE /reservations-waitings/{id}
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteMyReservationWaiting(HttpServletRequest req, @PathVariable Long id) {
        String userName = req.getHeader("Authorization");
        if (userName == null) {
            throw new MissingAuthorizationHeaderException();
        }
        
        reservationWaitingService.deleteReservationWaiting(id, userName);
    }
}
