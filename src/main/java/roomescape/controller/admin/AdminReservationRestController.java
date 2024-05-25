package roomescape.controller.admin;

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
import roomescape.service.ReservationService;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

@RestController
public class AdminReservationRestController {

    private final ReservationService reservationService;

    public AdminReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/admin/reservations/search")
    public List<ReservationResponse> searchReservations(
            @RequestParam(name = "status") String status,
            @RequestParam(name = "member", required = false) String email,
            @RequestParam(name = "theme", required = false) Long themeId,
            @RequestParam(name = "start-date", required = false) LocalDate startDate,
            @RequestParam(name = "end-date", required = false) LocalDate endDate) {
        ReservationSearchParams request = new ReservationSearchParams(status, email, themeId, startDate, endDate);
        return reservationService.searchReservations(request);
    }

    @GetMapping("/admin/reservations")
    public List<ReservationResponse> findAllReservationByStatus(@RequestParam String status) {
        return reservationService.findAllReservationsByStatus(status);
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
