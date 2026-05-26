package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.reservation.controller.dto.ReservationResponseDto;
import roomescape.reservation.controller.dto.UserReservationUpdateRequestDto;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getByName(@RequestParam String name) {
        List<ReservationResponseDto> body = reservationService.getByName(name).stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancelForUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> update(
            @PathVariable Long id, @RequestBody @Valid UserReservationUpdateRequestDto request) {
        ReservationResponseDto body = ReservationResponseDto.from(
                reservationService.update(id, request.timeId()));
        return ResponseEntity.ok(body);
    }
}
