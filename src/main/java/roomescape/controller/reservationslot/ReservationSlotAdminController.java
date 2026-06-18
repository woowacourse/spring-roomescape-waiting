package roomescape.controller.reservationslot;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.reservationslot.dto.ReservationSlotCreateRequest;
import roomescape.controller.reservationslot.dto.ReservationSlotResponse;
import roomescape.service.reservationslot.ReservationSlotService;

@RestController
@RequestMapping("/admin/reservation-slots")
public class ReservationSlotAdminController {
    private final ReservationSlotService reservationSlotService;

    public ReservationSlotAdminController(final ReservationSlotService reservationSlotService) {
        this.reservationSlotService = reservationSlotService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationSlotResponse>> getReservationSlots() {
        List<ReservationSlotResponse> slots = reservationSlotService.getAll().stream()
                .map(ReservationSlotResponse::from)
                .toList();
        return ResponseEntity.ok(slots);
    }

    @PostMapping
    public ResponseEntity<ReservationSlotResponse> openReservationSlot(
            @Valid @RequestBody final ReservationSlotCreateRequest request
    ) {
        ReservationSlotResponse slot = ReservationSlotResponse.from(reservationSlotService.open(
                request.date(),
                request.themeId(),
                request.timeId()
        ));

        return ResponseEntity.created(URI.create("/admin/reservation-slots/" + slot.id()))
                .body(slot);
    }
}
