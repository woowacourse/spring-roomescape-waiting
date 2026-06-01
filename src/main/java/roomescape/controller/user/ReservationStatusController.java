package roomescape.controller.user;


import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.ReservationStatusResponse;
import roomescape.service.ReservationLookupService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/reservation-statuses")
public class ReservationStatusController {
    private final ReservationLookupService reservationLookupService;

    public ReservationStatusController(ReservationLookupService reservationLookupService) {
        this.reservationLookupService = reservationLookupService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationStatusResponse>> getReservationStatuses(
            @RequestParam("name") @NotBlank(message = "name은 비어 있을 수 없습니다.") String name
    ) {
        List<ReservationStatusResponse> results = reservationLookupService.findByName(name)
                .stream().map(ReservationStatusResponse::from).toList();
        return ResponseEntity.ok(results);
    }
}
