package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;
import static roomescape.auth.domain.AuthRole.MEMBER;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByfilterRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReservationRestController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final CreateReservationRequest request,
            final MemberAuthInfo memberAuthInfo
    ) {
        final ReservationResponse response = reservationService.create(request, memberAuthInfo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/reservations/{id}")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<Void> delete(
            @PathVariable final Long id,
            final MemberAuthInfo memberAuthInfo
    ) {
        reservationService.deleteIfOwner(id, memberAuthInfo);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<List<ReservationResponse>> findAll() {
        final List<ReservationResponse> reservationResponses = reservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/filtered")
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<List<ReservationResponse>> findAllByFilter(
            @ModelAttribute @Valid final ReservationsByfilterRequest request
    ) {
        final List<ReservationResponse> reservationResponses = reservationService.findAllByFilter(request);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/available-times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableReservationTimes(
            @ModelAttribute @Valid final AvailableReservationTimeRequest request) {
        final List<AvailableReservationTimeResponse> availableReservationTimes =
                reservationService.findAvailableReservationTimes(request);

        return ResponseEntity.ok(availableReservationTimes);
    }
}
