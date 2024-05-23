package roomescape.controller.reservation;

import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.repository.dto.WaitingReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

@RestController
public class AdminReservationRestController {

    private final ReservationService reservationService;

    public AdminReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/admin/reservations/confirmed")
    public List<ReservationResponse> searchConfirmedReservations(
            @RequestParam(name = "member", required = false) String email,
            @RequestParam(name = "theme", required = false) Long themeId,
            @RequestParam(name = "start-date", required = false) LocalDate startDate,
            @RequestParam(name = "end-date", required = false) LocalDate endDate) {
        ReservationSearchParams request = new ReservationSearchParams(email, themeId, startDate, endDate, CONFIRMED);
        return reservationService.searchConfirmedReservations(request);
    }

    @GetMapping("/admin/reservations/waiting")
    public List<WaitingReservationResponse> findAllWaitingReservations() {
        return reservationService.findAllWaitingReservations();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/reservations")
    public ReservationResponse createReservation(@Valid @RequestBody AdminReservationRequest reservation) {
        return reservationService.createReservation(reservation.toCreateReservation());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/reservations/{id}")
    public void deleteConfirmedReservation(@PathVariable long id) {
        reservationService.rejectConfirmedReservation(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/reservations/waiting/{id}")
    public void deleteWaitingReservation(@PathVariable long id) {
        reservationService.rejectWaitingReservation(id);
    }
}
