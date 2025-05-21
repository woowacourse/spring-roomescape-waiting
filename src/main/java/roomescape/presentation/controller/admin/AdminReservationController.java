package roomescape.presentation.controller.admin;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationServiceResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService service;

    public AdminReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationServiceResponse> createReservation(
            @Valid @RequestBody ReservationCreateServiceRequest request) {
        ReservationServiceResponse reservationServiceResponse = service.registerReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationServiceResponse);
    }

    @GetMapping
    public List<ReservationServiceResponse> getReservationsMatching(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        return service.searchReservationsWith(themeId, memberId, dateFrom, dateTo);
    }
}
