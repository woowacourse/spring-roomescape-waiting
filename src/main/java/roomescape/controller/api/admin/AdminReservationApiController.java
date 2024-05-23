package roomescape.controller.api.admin;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.service.dto.request.ReservationAdminSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.reservation.AdminReservationCreateService;
import roomescape.service.reservation.ReservationFindService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class AdminReservationApiController {

    private final AdminReservationCreateService adminReservationCreateService;
    private final ReservationFindService reservationFindService;

    public AdminReservationApiController(AdminReservationCreateService adminReservationCreateService,
                                         ReservationFindService reservationFindService) {
        this.adminReservationCreateService = adminReservationCreateService;
        this.reservationFindService = reservationFindService;
    }

    @GetMapping("/api/admin/reservations/search")
    public ResponseEntity<List<ReservationResponse>> getSearchingReservations(@RequestParam long memberId,
                                                                              @RequestParam long themeId,
                                                                              @RequestParam LocalDate dateFrom,
                                                                              @RequestParam LocalDate dateTo) {
        List<Reservation> reservations = reservationFindService.searchReservations(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(
                reservations.stream()
                        .map(ReservationResponse::new)
                        .toList()
        );
    }

    @PostMapping("/api/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservationByAdmin(@RequestBody @Valid
                                                                     ReservationAdminSaveRequest request) {
        Reservation newReservation = adminReservationCreateService.createReservation(request);
        return ResponseEntity.created(URI.create("/api/admin/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }
}
