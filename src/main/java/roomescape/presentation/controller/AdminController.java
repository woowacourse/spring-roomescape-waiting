package roomescape.presentation.controller;

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
import roomescape.business.service.ReservationService;
import roomescape.presentation.dto.ReservationRequest;
import roomescape.presentation.dto.ReservationResponse;
import roomescape.presentation.dto.WaitResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody final ReservationRequest reservationRequest
    ) {
        final ReservationResponse reservationResponse = reservationService.insert(
                reservationRequest.memberId(),
                reservationRequest.themeId(),
                reservationRequest.date(),
                reservationRequest.timeId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationResponse);
    }

    @GetMapping("/reservations/filter")
    public ResponseEntity<List<ReservationResponse>> readFilter(
            @RequestParam(required = false) final Long memberId,
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final LocalDate dateFrom,
            @RequestParam(required = false) final LocalDate dateTo
    ) {
        final List<ReservationResponse> reservationResponses = reservationService.findAllFilter(
                memberId, themeId, dateFrom, dateTo
        );

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/waits")
    public ResponseEntity<List<WaitResponse>> readWaits() {
        final List<WaitResponse> waitResponses = reservationService.findWaitInfoByStatusNotApprove();

        return ResponseEntity.ok(waitResponses);
    }
}
