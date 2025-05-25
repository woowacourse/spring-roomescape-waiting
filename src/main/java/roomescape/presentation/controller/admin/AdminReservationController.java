package roomescape.presentation.controller.admin;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationCommandService;
import roomescape.application.ReservationQueryService;
import roomescape.application.WaitingService;
import roomescape.application.dto.ReservationCreateDto;
import roomescape.application.dto.ReservationDto;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final WaitingService waitingService;

    public AdminReservationController(ReservationCommandService reservationCommandService,
                                      ReservationQueryService reservationQueryService,
                                      WaitingService waitingService) {
        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(@Valid @RequestBody ReservationCreateDto request) {
        ReservationDto reservationDto = reservationCommandService.registerReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationDto);
    }

    @GetMapping
    public List<ReservationDto> getReservationsMatching(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        return reservationQueryService.searchReservationsWith(themeId, memberId, dateFrom, dateTo);
    }

    @GetMapping("/waiting")
    public List<ReservationDto> getAllWaitings() {
        return waitingService.getAllWaitings();
    }

    @PatchMapping("/waiting/{id}/accept")
    public void acceptReserve(@PathVariable("id") Long reservationId) {
        waitingService.acceptReserve(reservationId);
    }
}
