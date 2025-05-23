package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;
import static roomescape.auth.domain.AuthRole.MEMBER;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import roomescape.reservation.ui.dto.request.CreateWaitingRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationResponse;

@RestController
@RequiredArgsConstructor
public class ReservationRestController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final CreateReservationRequest.ForMember request,
            final MemberAuthInfo memberAuthInfo
    ) {
        final ReservationResponse response = reservationService.createConfirmedReservation(request,
                memberAuthInfo.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/reservations/{id}")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<Void> delete(
            @PathVariable final Long id,
            final MemberAuthInfo memberAuthInfo
    ) {
        reservationService.deleteIfOwner(id, memberAuthInfo.id());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations/mine")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<List<ReservationResponse.ForMember>> findAllMyReservations(
            final MemberAuthInfo memberAuthInfo
    ) {
        return ResponseEntity.ok()
                .body(reservationService.findReservationsByMemberId(memberAuthInfo.id()));
    }

    @GetMapping("/reservations/available-times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAllAvailableReservationTimes(
            @ModelAttribute @Valid final AvailableReservationTimeRequest request
    ) {
        final List<AvailableReservationTimeResponse> availableReservationTimes =
                reservationService.findAvailableReservationTimes(request);

        return ResponseEntity.ok(availableReservationTimes);
    }

    @PostMapping("/waitings")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<ReservationResponse> createWaiting(
            @RequestBody @Valid final CreateWaitingRequest.ForMember request,
            final MemberAuthInfo memberAuthInfo
    ) {
        final ReservationResponse response = reservationService.createWaitingReservation(request, memberAuthInfo.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/waitings/{id}")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable final Long id,
            final MemberAuthInfo memberAuthInfo
    ) {
        reservationService.deleteIfOwner(id, memberAuthInfo.id());

        return ResponseEntity.noContent().build();
    }
}
