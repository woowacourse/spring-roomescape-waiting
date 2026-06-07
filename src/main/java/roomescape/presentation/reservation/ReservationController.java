package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationService;
import roomescape.presentation.reservation.request.ReservationCreateRequest;
import roomescape.presentation.reservation.request.ReservationUpdateRequest;
import roomescape.presentation.reservation.response.ReservationCreateResponse;
import roomescape.presentation.reservation.response.ReservationUpdateResponse;
import roomescape.presentation.reservation.response.UserReservationsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<UserReservationsResponse> getUserReservations(
            @RequestParam
            @NotBlank(message = "예약자 이름은 필수 입력값 입니다.")
            String name
    ) {
        UserReservationsResponse response = reservationService.getUserReservations(name);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            HttpSession session
    ) {
        ReservationCreateResponse response = reservationService.createReservationByUser(request, getUsername(session));
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationUpdateResponse> updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationUpdateRequest request,
            HttpSession session
    ) {
        ReservationUpdateResponse response = reservationService.updateReservationByUser(id, request, getUsername(session));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            HttpSession session
    ) {
        reservationService.cancelReservationByUser(id, getUsername(session));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private String getUsername(HttpSession session) {
        Object username = session.getAttribute("username");
        if (!(username instanceof String value) || value.isBlank()) {
            throw new IllegalStateException("세션 사용자명이 필요합니다.");
        }
        return value;
    }
}
