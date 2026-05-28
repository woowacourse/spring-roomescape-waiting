package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.ReservationTime;
import roomescape.web.dto.request.ReservationTimeRequest;
import roomescape.web.dto.response.ReservationTimeResponse;
import roomescape.service.ReservationTimeCommandService;
import roomescape.service.ReservationTimeQueryService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/times")
@RequiredArgsConstructor
public class AdminTimeController {

    private final ReservationTimeCommandService reservationTimeCommandService;
    private final ReservationTimeQueryService reservationTimeQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAllTimes() {
        List<ReservationTime> allReservationTimes = reservationTimeQueryService.findAllReservationTimes();
        List<ReservationTimeResponse> reservationTimeResponse = allReservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(reservationTimeResponse);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(@Valid @RequestBody ReservationTimeRequest request) {
        ReservationTime reservationTime = reservationTimeCommandService.create(request.startAt());
        Long savedId = reservationTime.id();

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location)
                .body(ReservationTimeResponse.from(reservationTime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("id") Long id) {
        reservationTimeCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
