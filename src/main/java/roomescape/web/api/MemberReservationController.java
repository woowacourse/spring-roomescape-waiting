package roomescape.web.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.member.MemberInfo;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.UserReservationRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.UserReservationResponse;

@RestController
@RequiredArgsConstructor
public class MemberReservationController {
    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> makeReservation(
            @RequestBody @Valid UserReservationRequest request,
            MemberInfo memberInfo
    ) {
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .date(request.date()).memberId(memberInfo.id())
                .timeId(request.timeId()).themeId(request.themeId())
                .build();

        Reservation reservation = reservationService.saveReservation(reservationRequest);

        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
                .body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAllMemberReservations() {
        List<ReservationResponse> response = reservationService.findAllReservation();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<UserReservationResponse>> findAllMyReservations(MemberInfo memberInfo) {
        List<UserReservationResponse> reservations = reservationService.findAllWithRank(memberInfo.id());
        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/reservations/{idReservation}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable(value = "idReservation") Long reservationId,
            MemberInfo memberInfo
    ) {
        reservationService.cancelReservation(reservationId, memberInfo);
        return ResponseEntity.noContent().build();
    }
}
