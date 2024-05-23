package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.auth.AuthService;
import roomescape.service.schedule.ReservationTimeService;
import roomescape.service.schedule.dto.AvailableReservationTimeResponse;
import roomescape.service.schedule.dto.ReservationTimeCreateRequest;
import roomescape.service.schedule.dto.ReservationTimeReadRequest;
import roomescape.service.schedule.dto.ReservationTimeResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;
    private final AuthService authService;

    public ReservationTimeController(ReservationTimeService reservationTimeService, AuthService authService) {
        this.reservationTimeService = reservationTimeService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @RequestBody @Valid ReservationTimeCreateRequest reservationTimeCreateRequest) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(reservationTimeCreateRequest);
        return ResponseEntity.created(URI.create("/times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @GetMapping
    public List<ReservationTimeResponse> findAllReservationTimes() {
        return reservationTimeService.findAll();
    }

    @GetMapping("/available")
    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            @ModelAttribute("ReservationTimeReadRequest") ReservationTimeReadRequest reservationTimeReadRequest) {
        return reservationTimeService.findAvailableTimes(reservationTimeReadRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTimeById(@PathVariable("id") long timeId, @LoginMemberId long memberId) {
        authService.validateAdmin(memberId);
        reservationTimeService.deleteById(timeId);
        return ResponseEntity.noContent().build();
    }
}
