package roomescape.controller.api.admin;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.service.dto.request.ReservationAdminSaveRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.ReservationResponses;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationFindService;

@RestController
public class AdminReservationApiController {

    private final ReservationFindService reservationFindService;
    private final ReservationCreateService reservationCreateService;

    public AdminReservationApiController(ReservationFindService reservationFindService,
                                         ReservationCreateService reservationCreateService) {
        this.reservationFindService = reservationFindService;
        this.reservationCreateService = reservationCreateService;
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<ReservationResponses> getReservations() {
        List<Reservation> reservations = reservationFindService.findReservations();
        return ResponseEntity.ok(ReservationResponses.from(reservations));
    }

    @GetMapping("/admin/reservations/search")
    public ResponseEntity<ReservationResponses> getSearchingReservations(@RequestParam long memberId,
                                                                         @RequestParam long themeId,
                                                                         @RequestParam LocalDate dateFrom,
                                                                         @RequestParam LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationFindService.searchReservations(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(ReservationResponses.from(reservations));
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid ReservationAdminSaveRequest request) {
        Reservation newReservation = reservationCreateService.createReservation(request);
        return ResponseEntity.created(URI.create("/admin/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }
}
