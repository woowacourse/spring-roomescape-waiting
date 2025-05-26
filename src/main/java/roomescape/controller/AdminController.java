package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.dto.AdminReservationWaitingResponse;
import roomescape.service.dto.ReservationRecipe;
import roomescape.service.dto.ReservationResponse;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid final ReservationRecipe recipe) {
        ReservationResponse response = reservationService.addReservation(recipe);
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

    @GetMapping("/admin/reservations-waiting")
    public ResponseEntity<List<AdminReservationWaitingResponse>> getReservationsWaiting() {
        return ResponseEntity.status(HttpStatus.OK).body(reservationWaitingService.getAllReservationWaiting());
    }

    @DeleteMapping("/admin/reservations-waiting/{id}")
    public ResponseEntity<Void> removeReservationWaiting(@PathVariable final long id) {
        reservationWaitingService.removeReservationWaiting(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
