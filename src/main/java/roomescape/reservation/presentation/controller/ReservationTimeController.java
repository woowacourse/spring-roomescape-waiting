package roomescape.reservation.presentation.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.application.service.ReservationTimeService;
import roomescape.reservation.presentation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @Auth(Role.ADMIN)
    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createTime(
            final @RequestBody ReservationTimeRequest reservationTimeRequest
    ) {
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        return ResponseEntity.created(createUri(reservationTime.getId()))
                .body(reservationTime);
    }

    @Auth(Role.USER)
    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes(
    ) {
        return ResponseEntity.ok().body(
                reservationTimeService.getReservationTimes()
        );
    }

    @Auth(Role.USER)
    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getReservationTimes(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        return ResponseEntity.ok().body(
                reservationTimeService.getReservationTimes(date, themeId)
        );
    }

    @Auth(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(
            final @PathVariable Long id
    ) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }

    private URI createUri(Long timeId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(timeId)
                .toUri();
    }
}
