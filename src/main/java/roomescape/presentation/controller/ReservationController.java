package roomescape.presentation.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.business.service.PlayTimeService;
import roomescape.business.service.ReservationService;
import roomescape.presentation.dto.LoginMember;
import roomescape.presentation.dto.ReservationAvailableTimeResponse;
import roomescape.presentation.dto.ReservationRequest;
import roomescape.presentation.dto.ReservationResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final PlayTimeService playTimeService;

    public ReservationController(final ReservationService reservationService, final PlayTimeService playTimeService) {
        this.reservationService = reservationService;
        this.playTimeService = playTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createByLoginMember(
            @RequestBody final ReservationRequest reservationRequest,
            final LoginMember loginMember
    ) {
        final ReservationResponse reservationResponse = reservationService.insert(
                reservationRequest.date(),
                loginMember.id(),
                reservationRequest.timeId(),
                reservationRequest.themeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readAll() {
        final List<ReservationResponse> reservationResponses = reservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/filter")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        reservationService.deleteById(id);

        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<ReservationAvailableTimeResponse>> readAvailableTimes(
            @RequestParam("date") final LocalDate date,
            @RequestParam("themeId") final Long themeId
    ) {
        final List<ReservationAvailableTimeResponse> availableTimeResponses =
                playTimeService.findAvailableTimes(date, themeId);

        return ResponseEntity.ok(availableTimeResponses);
    }
}
