package roomescape.controller.admin;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.response.ReservationResponse;
import roomescape.model.Reservation;
import roomescape.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> searchReservations(@RequestParam("themeId") Long themeId,
                                                                        @RequestParam("memberId") Long memberId,
                                                                        @RequestParam("dateFrom") LocalDate dateFrom,
                                                                        @RequestParam("dateTo") LocalDate dateTo) {
        List<Reservation> reservations = reservationService.filterReservation(themeId, memberId, dateFrom, dateTo);
        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::new)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<Reservation> createReservation(@RequestBody AdminReservationRequest request) {
        Reservation reservation = reservationService.addReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservation);
    }
}
