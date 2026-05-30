package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.presentation.dto.ReservationApplicationCreateRequest;
import roomescape.reservation.presentation.dto.ReservationApplicationResponse;
import roomescape.reservation.presentation.dto.ReservationUpdateRequest;

@RequiredArgsConstructor
@Validated
@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationApplicationResponse>> findByName(
            @NotBlank(message = "이름은 비어있을 수 없습니다.")
            @RequestParam String username
    ) {
        List<ReservationApplicationResult> results = reservationQueryService.findByName(username);

        List<ReservationApplicationResponse> responses = results.stream()
                .map(ReservationApplicationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationApplicationResponse> save(
            @Valid @RequestBody ReservationApplicationCreateRequest request
    ) {
        ReservationApplicationCreateCommand createCommand = request.toCommand(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationApplicationResponse.from(reservationCommandService.save(createCommand)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationApplicationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationUpdateCommand updateCommand = request.toCommand(LocalDateTime.now());

        return ResponseEntity.ok(
                ReservationApplicationResponse.from(reservationCommandService.update(id, updateCommand)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        reservationCommandService.delete(id, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }
}
