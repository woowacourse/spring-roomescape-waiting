package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid final AdminReservationRequest request) {
        ReservationResponse response = reservationService.addReservationForAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByFilter(
            @RequestParam(required = false, name = "memberId") Long memberId,
            @RequestParam(required = false, name = "themeId") Long themeId,
            @RequestParam(required = false, name = "dateFrom") LocalDate dateFrom,
            @RequestParam(required = false, name = "dateTo") LocalDate dateTo
    ) {
        return ResponseEntity.status(HttpStatus.OK).body( reservationService.getFilteredReservations(memberId, themeId, dateFrom, dateTo));
    }
}
