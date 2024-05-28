package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.CreateReservationRequest;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.FindReservationResponse;
import roomescape.controller.dto.FindReservationStandbyResponse;
import roomescape.controller.dto.SearchReservationFilterRequest;
import roomescape.service.AdminReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @PostMapping
    public ResponseEntity<CreateReservationResponse> save(@Valid @RequestBody CreateReservationRequest request) {
        CreateReservationResponse response = adminReservationService.reserve(
            request.memberId(),
            request.date(),
            request.timeId(),
            request.themeId()
        );

        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
            .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminReservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FindReservationResponse>> findAll() {
        List<FindReservationResponse> response = adminReservationService.findAllReserved();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/standby")
    public ResponseEntity<List<FindReservationStandbyResponse>> findAllStandby() {
        List<FindReservationStandbyResponse> response = adminReservationService.findAllStandby();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/standby/{id}")
    public ResponseEntity<Void> deleteStandby(@PathVariable Long id) {
        adminReservationService.deleteStandby(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FindReservationResponse>> find(SearchReservationFilterRequest request) {
        List<FindReservationResponse> response = adminReservationService.findAllByFilter(
            request.themeId(), request.memberId(), request.dateFrom(), request.dateTo());
        return ResponseEntity.ok(response);
    }
}
