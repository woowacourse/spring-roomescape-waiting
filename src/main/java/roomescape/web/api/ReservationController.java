package roomescape.web.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.member.MemberInfo;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.ReservationSearchCond;
import roomescape.service.dto.request.reservation.UserReservationRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.UserReservationResponse;

@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(
            @RequestBody @Valid UserReservationRequest request,
            MemberInfo memberInfo
    ) {
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .date(request.date())
                .memberId(memberInfo.id())
                .timeId(request.timeId())
                .themeId(request.themeId())
                .build();

        Reservation reservation = reservationService.saveMemberReservation(reservationRequest);

        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
                .body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAllReservation() {
        List<ReservationResponse> response = reservationService.findAllReservation();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> findAllReservationByConditions(
            @RequestParam("from") LocalDate start,
            @RequestParam("to") LocalDate end,
            @RequestParam("memberId") Long memberId,
            @RequestParam("themeId") Long themeId
    ) {
        ReservationSearchCond searchCond = new ReservationSearchCond(start, end, memberId, themeId);
        List<ReservationResponse> reservations = reservationService.findAllReservationByConditions(searchCond);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<UserReservationResponse>> findAllByMemberId(MemberInfo memberInfo) {
        List<UserReservationResponse> reservations = reservationService.findAllByMemberId(memberInfo.id());

        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/reservations/{reservation_id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable(value = "reservation_id") Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
