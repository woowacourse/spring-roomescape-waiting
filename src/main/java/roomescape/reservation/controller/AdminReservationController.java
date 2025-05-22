package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse saveReservation(@Valid @RequestBody final AdminReservationRequest request) {
        return reservationService.saveAdminReservation(request);
    }

    @GetMapping("/waiting")
    public List<ReservationResponse> getWaitingReservations() {
        return reservationService.findAllWaitingReservation();
    }

    @PatchMapping("/{id}")
    public void approveWaitingReservation(@PathVariable @NotNull final Long id) {
        reservationService.approveWaitingReservation(id);
    }

    @DeleteMapping("/{id}")
    public void deleteWaitingReservation(@PathVariable @NotNull final Long id) {
        reservationService.deleteReservation(id);
    }
}
