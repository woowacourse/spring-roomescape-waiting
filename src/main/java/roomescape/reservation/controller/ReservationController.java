package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ReservationCreateResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.UserReservationsResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.dto.ReservedTimeResponse;

@RestController
@RequestMapping("/api/reservations")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> add(
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationCreateResponse saved = reservationService.reserve(request);
        return ResponseEntity.created(URI.create("/api/reservations/" + saved.id())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> readById(
            @PathVariable @Positive(message = "예약 아이디는 1 이상이어야 합니다.") Long id
    ) {
        return ResponseEntity.ok(reservationService.readById(id));
    }

    @GetMapping
    public ResponseEntity<UserReservationsResponse> findUserReservations(
            @RequestParam @NotBlank(message = "이름은 한 글자 이상이어야 합니다.") String name
    ) {
        return ResponseEntity.ok(reservationService.findUserReservations(name));
    }

    @GetMapping("/booked-times")
    public ResponseEntity<List<ReservedTimeResponse>> findReservedTimes(
            @RequestParam LocalDate selectedDate, @RequestParam Long themeId
    ) {
        return ResponseEntity.ok(reservationService.findReservedTimes(selectedDate, themeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "예약 아이디는 1 이상이어야 합니다.") Long id
    ) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
