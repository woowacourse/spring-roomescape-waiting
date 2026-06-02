package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationTimeCreateRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

import java.net.URI;

@RequestMapping("/api/v1/admin/reservation-times")
@RestController
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @Valid @RequestBody ReservationTimeCreateRequest reservationTimeCreateRequest,
            @LoginMember(role = Role.MANAGER) Member manager) {
        ReservationTime savedReservationTime = reservationTimeService.createReservationTime(
                reservationTimeCreateRequest.startAt());
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(savedReservationTime);
        return ResponseEntity.created(URI.create("/api/v1/admin/reservation-times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(
            @PathVariable Long id,
            @LoginMember(role = Role.MANAGER) Member manager) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
