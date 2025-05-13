package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.annotation.CheckRole;
import roomescape.entity.ReservationSlot;
import roomescape.entity.ReservationSlots;
import roomescape.entity.ReservationTime;
import roomescape.entity.Role;
import roomescape.dto.request.AvailableTimeRequest;
import roomescape.dto.request.CreateReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ReservationTimeSlotResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAll();
        List<ReservationTimeResponse> responses = reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ReservationTimeSlotResponse>> getAvailableReservationTimes(
            @ModelAttribute @Valid AvailableTimeRequest request
    ) {
        ReservationSlots reservationSlotTimes = reservationTimeService.getReservationSlots(request);
        List<ReservationSlot> reservationSlots = reservationSlotTimes.getReservationSlots();
        List<ReservationTimeSlotResponse> responses = reservationSlots.stream()
                .map(ReservationTimeSlotResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @CheckRole(value = Role.ADMIN)
    public ResponseEntity<ReservationTimeResponse> addReservationTime(
            @RequestBody @Valid CreateReservationTimeRequest request
    ) {
        ReservationTime reservationTime = reservationTimeService.addReservationTime(request);
        ReservationTimeResponse response = ReservationTimeResponse.from(reservationTime);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @CheckRole(value = Role.ADMIN)
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);

        return ResponseEntity.noContent().build();
    }
}
