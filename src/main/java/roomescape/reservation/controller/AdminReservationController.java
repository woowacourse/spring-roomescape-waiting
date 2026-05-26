package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.controller.dto.ReservationResponseDto;
import roomescape.reservation.controller.dto.ReservationSaveRequestDto;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getAll() {
        List<ReservationResponseDto> body = reservationService.getAll().stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> create(
            @RequestBody @Valid ReservationSaveRequestDto reservationRequest) {
        ReservationResponseDto body = ReservationResponseDto.from(
                reservationService.create(reservationRequest.toServiceDto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
