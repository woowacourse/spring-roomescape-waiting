package roomescape.reservation.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.common.PageResponse;
import roomescape.reservation.AdminReservationService;
import roomescape.reservation.Reservation;

@Validated
@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final AdminReservationService reservationService;

    public AdminReservationController(AdminReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminReservationResponseDto>> findAll(
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<AdminReservationResponseDto> responses = reservationService.findAll(page, size)
                .map(AdminReservationResponseDto::from);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminReservationResponseDto> findById(@PathVariable Long id) {
        Reservation reservation = reservationService.findById(id);
        return ResponseEntity.ok(AdminReservationResponseDto.from(reservation));
    }

    @PostMapping
    public ResponseEntity<AdminReservationResponseDto> create(
            @Valid @RequestBody AdminReservationRequestDto request
    ) {
        Reservation reservation = reservationService.createByAdmin(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();
        return ResponseEntity.created(uri).body(AdminReservationResponseDto.from(reservation));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminReservationResponseDto> patch(
            @PathVariable Long id,
            @Valid @RequestBody ReservationPatchDto request
    ) {
        Reservation updated = reservationService.update(id, request);
        return ResponseEntity.ok(AdminReservationResponseDto.from(updated));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancelByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
