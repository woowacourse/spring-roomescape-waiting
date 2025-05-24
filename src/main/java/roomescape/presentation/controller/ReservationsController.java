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
import roomescape.business.service.ReservationService;
import roomescape.business.service.ReservationTimeService;
import roomescape.config.AuthenticationPrincipal;
import roomescape.presentation.dto.LoginMember;
import roomescape.presentation.dto.ReservationAvailableTimeResponse;
import roomescape.presentation.dto.ReservationRequest;
import roomescape.presentation.dto.ReservationResponse;
import roomescape.presentation.dto.WaitInfoResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationsController {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;

    public ReservationsController(
            final ReservationService reservationService,
            final ReservationTimeService reservationTimeService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createByLoginMember(
            @AuthenticationPrincipal final LoginMember loginMember,
            @RequestBody final ReservationRequest reservationRequest
    ) {
        final ReservationResponse reservationResponse = reservationService.insert(
                reservationRequest.date(),
                loginMember.id(),
                reservationRequest.timeId(),
                reservationRequest.themeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationResponse);
    }

    @PostMapping("/wait")
    public ResponseEntity<WaitInfoResponse> createReservationWaitByLoginMember(
            @AuthenticationPrincipal final LoginMember loginMember,
            @RequestBody final ReservationRequest reservationRequest
    ) {
        final WaitInfoResponse waitInfoResponse = reservationService.insertWait(
                reservationRequest.date(),
                loginMember.id(),
                reservationRequest.timeId(),
                reservationRequest.themeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(waitInfoResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readAll() {
        final List<ReservationResponse> reservationResponses = reservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    // TODO: 컨트롤러 이동 및 테스트 변경, 사유: 어드민 페이지에서만 사용 가능함.
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
                reservationTimeService.findAvailableTimes(date, themeId);

        return ResponseEntity.ok(availableTimeResponses);
    }
}
