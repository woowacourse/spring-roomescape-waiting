package roomescape.reservation.presentation.controller.regular;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.ConfirmedReservationApplicationService;
import roomescape.reservation.application.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservation.presentation.dto.request.ConfirmedReservationCreateWebRequest;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationWebResponse;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;

@RestController
public class RegularConfirmedReservationController {

    private final ConfirmedReservationApplicationService confirmedReservationApplicationService;

    public RegularConfirmedReservationController(
            final ConfirmedReservationApplicationService confirmedReservationApplicationService) {
        this.confirmedReservationApplicationService = confirmedReservationApplicationService;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/reservations")
    public ResponseEntity<ConfirmedReservationWebResponse> create(
            @RequestBody ConfirmedReservationCreateWebRequest request,
            MemberInfo memberInfo
    ) {
        ConfirmedReservationWebResponse response = confirmedReservationApplicationService.create(
                ConfirmedReservationCreateRequest.of(request, memberInfo));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/my-reservations")
    public ResponseEntity<List<MyReservationResponse>> findReservationsByMemberId(MemberInfo memberInfo) {
        List<MyReservationResponse> myReservations = confirmedReservationApplicationService.findReservationsByMemberId(
                memberInfo.id());
        return ResponseEntity.ok().body(myReservations);
    }
}
