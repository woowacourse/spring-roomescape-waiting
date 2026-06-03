package roomescape.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import roomescape.auth.Authorized;
import roomescape.auth.OwnerOnly;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class MyReservationController {

    private final ReservationService reservationService;

    public MyReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Authorized
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{id}")
    public void updateMyReservation(
            @OwnerOnly String name,
            @PathVariable Long id,
            @RequestBody ReservationUpdateRequest request
    ) {
        reservationService.update(request.toCommand(), id, name, LocalDateTime.now());
    }

    @Authorized
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteMyReservation(@OwnerOnly String name, @PathVariable Long id) {
        reservationService.deleteById(id, name, LocalDateTime.now());
    }
}
