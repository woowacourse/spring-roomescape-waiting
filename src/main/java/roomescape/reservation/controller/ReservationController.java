package roomescape.reservation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ReservationCreateResponse;
import roomescape.reservation.dto.ReservationIdResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.service.ReservationService;

@Tag(name = "예약", description = "예약 생성·조회·수정·삭제 API")
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationsByName(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(reservationService.getReservationsByName(name));
    }

    @GetMapping("/id")
    public ResponseEntity<ReservationIdResponse> getReservationId(@RequestParam LocalDate date,
                                                                  @RequestParam Long themeId,
                                                                  @RequestParam Long timeId) {
        return ResponseEntity.ok(reservationService.getReservationId(date, themeId, timeId));
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationCreateResponse response = reservationService.createReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}