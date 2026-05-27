package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.reservation.controller.dto.ReservationsResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest) {
        Reservation reservation = reservationService.create(
                reservationCreateRequest.name(),
                reservationCreateRequest.date(),
                reservationCreateRequest.timeId(),
                reservationCreateRequest.themeId()
        );

        return ResponseEntity.status(CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping
    public ResponseEntity<ReservationsResponse> listByName(
            @NotBlank(message = "예약자 이름은 비어 있을 수 없습니다.")
            @RequestParam("name") String name
    ) {
        List<ReservationResponse> reservations = reservationService.findByName(name)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(ReservationsResponse.from(reservations));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateDateTime(
            @Positive(message = "예약 id는 1 이상의 숫자여야 합니다.")
            @PathVariable Long id,

            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        Reservation reservation = reservationService.updateDateTime(
                id,
                request.name(),
                request.date(),
                request.timeId()
        );

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @Positive(message = "예약 id는 1 이상의 숫자여야 합니다.")
            @PathVariable Long id,

            @NotBlank(message = "예약자 이름은 비어 있을 수 없습니다.")
            @RequestParam("name") String name
    ) {
        reservationService.cancel(id, name);
        return ResponseEntity.noContent().build();
    }
}
