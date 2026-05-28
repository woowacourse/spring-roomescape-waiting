package roomescape.controller;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ReservationRequestDTO;
import roomescape.dto.ReservationResponseDTO;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservedTimeResponseDTO;
import roomescape.dto.UserBookingResponseDTO;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Void> add(@Valid @RequestBody ReservationRequestDTO request) {
        ReservationResponseDTO saved = reservationService.addReservation(request);
        return ResponseEntity.created(URI.create("/api/reservations/" + saved.id())).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> readById(
            @PathVariable @Positive(message = "예약 아이디는 1 이상이어야 합니다.") Long id) {
        return ResponseEntity.ok(reservationService.readReservationById(id));
    }

    @GetMapping
    public ResponseEntity<UserBookingResponseDTO> findUserBookingByName(
            @RequestParam @NotBlank(message = "이름은 한 글자 이상이어야 합니다.") String name) {
        return ResponseEntity.ok(reservationService.findReservationsAndWaitingByName(name));
    }

    @GetMapping("/booked-times")
    public ResponseEntity<List<ReservedTimeResponseDTO>> findReservedTimes(
            @RequestParam LocalDate selectedDate, @RequestParam Long themeId) {
        return ResponseEntity.ok(reservationService.findReservedTimes(selectedDate, themeId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable @Positive(message = "예약 아이디는 1 이상이어야 합니다.") Long id,
            @Valid @RequestBody ReservationUpdateRequest request) {
        ReservationResponseDTO updated = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "예약 아이디는 1 이상이어야 합니다.") Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
