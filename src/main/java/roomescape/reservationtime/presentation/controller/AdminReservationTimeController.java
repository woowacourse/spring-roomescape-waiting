package roomescape.reservationtime.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.application.service.ReservationTimeQueryService;
import roomescape.reservationtime.application.service.ReservationTimeCommandService;
import roomescape.reservationtime.presentation.dto.ReservationTimeCreateRequest;
import roomescape.reservationtime.presentation.dto.ReservationTimeResponse;

@RequiredArgsConstructor
@RequestMapping("/admin/times")
@RestController
public class AdminReservationTimeController {

    private final ReservationTimeCommandService timeCommandService;
    private final ReservationTimeQueryService timeQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        return ResponseEntity.ok(
                timeQueryService.findAll().stream()
                        .map(ReservationTimeResponse::from)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> save(
            @Valid @RequestBody ReservationTimeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationTimeResponse.from(timeCommandService.save(request.toCommand())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
