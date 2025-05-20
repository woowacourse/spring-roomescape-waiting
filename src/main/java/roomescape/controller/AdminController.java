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
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationRecipe;
import roomescape.service.dto.ReservationResponse;

@RestController
public class AdminController {

    private final ReservationService reservationServiceV2;

    public AdminController(final ReservationService reservationServiceV2) {
        this.reservationServiceV2 = reservationServiceV2;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid final ReservationRecipe recipe) {
        ReservationResponse response = reservationServiceV2.addReservation(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByFilter(
            @RequestParam(required = false, name = "memberId") Long memberId,
            @RequestParam(required = false, name = "themeId") Long themeId,
            @RequestParam(required = false, name = "dateFrom") LocalDate dateFrom,
            @RequestParam(required = false, name = "dateTo") LocalDate dateTo
    ) {
        return ResponseEntity.status(HttpStatus.OK).body( reservationServiceV2.getFilteredReservations(memberId, themeId, dateFrom, dateTo));
    }
}
