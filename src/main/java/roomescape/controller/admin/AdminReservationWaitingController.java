package roomescape.controller.admin;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationWaitingService;

@Validated
@RestController
@RequestMapping("/admin/waitings")
public class AdminReservationWaitingController {

    private final ReservationWaitingService service;

    public AdminReservationWaitingController(ReservationWaitingService service) {
        this.service = service;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable @Positive(message = "id는 양수이어야 합니다.") Long id) {
        service.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
