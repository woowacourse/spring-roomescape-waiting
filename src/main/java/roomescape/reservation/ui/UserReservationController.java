package roomescape.reservation.ui;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.login.application.dto.LoginCheckRequest;
import roomescape.reservation.application.ReservationCommandService;
import roomescape.reservation.application.ReservationQueryService;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MemberWaitingRequest;
import roomescape.reservation.application.dto.MyHistoryResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.WaitingResponse;

@RestController
public class UserReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    public UserReservationController(
            final ReservationCommandService reservationCommandService,
            final ReservationQueryService reservationQueryService
    ) {
        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyHistoryResponse>> findMyReservation(final LoginCheckRequest request) {
        List<MyHistoryResponse> response = reservationQueryService.findMyReservation(request.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservations/themes/{themeId}/times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableReservationTime(
            @PathVariable final Long themeId,
            @RequestParam final LocalDate date
    ) {
        return ResponseEntity.ok(reservationQueryService.findAvailableReservationTime(themeId, date));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> add(
            @Valid @RequestBody final MemberReservationRequest request,
            final LoginCheckRequest loginCheckRequest
    ) {
        final ReservationResponse reservationResponse = reservationCommandService.addMemberReservation(request,
                loginCheckRequest.id());
        return new ResponseEntity<>(reservationResponse, HttpStatus.CREATED);
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> add(
            @Valid @RequestBody final MemberWaitingRequest request,
            final LoginCheckRequest loginCheckRequest
    ) {
        final WaitingResponse waitingResponse = reservationCommandService.addMemberWaiting(request,
                loginCheckRequest.id());
        return new ResponseEntity<>(waitingResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("waitings/{id}")
    public ResponseEntity<Void> cancelWaiting(
            @PathVariable("id") final Long id,
            final LoginCheckRequest request
    ) {
        reservationCommandService.cancelOwnWaitingById(id, request.id());
        return ResponseEntity.noContent().build();
    }
}
