package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.domain.AuthInfo;
import roomescape.global.annotation.LoginUser;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String getPopularPage() {
        return "index";
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> reservations(
            @RequestParam(value = "themeId", required = false) Long themeId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "dateFrom", required = false) LocalDate startDate,
            @RequestParam(value = "dateTo", required = false) LocalDate endDate
    ) {
        return ResponseEntity.ok(reservationService.findMemberReservations(
                new ReservationQueryRequest(themeId, memberId, startDate, endDate)));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@LoginUser AuthInfo authInfo,
                                                      @RequestBody @Valid ReservationRequest reservationRequest) {
        ReservationResponse response = reservationService.createMemberReservation(authInfo, reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.memberReservationId())).body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@LoginUser AuthInfo authInfo,
                                       @PathVariable("id") @Min(1) long reservationMemberId) {
        reservationService.deleteMemberReservation(authInfo, reservationMemberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }


    @GetMapping("/reservation-mine")
    public String getMyPage() {
        return "reservation-mine";
    }

    @GetMapping("/reservations/my")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@LoginUser AuthInfo authInfo) {
        return ResponseEntity.ok(reservationService.findMyReservations(authInfo));
    }
}
