package roomescape.admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.AdminReservationResponse;
import roomescape.admin.dto.ReservationSearchRequest;
import roomescape.admin.dto.ReservationWaitingResponse;
import roomescape.admin.service.facade.AdminService;

@RequestMapping("/admin")
@RequiredArgsConstructor
@RestController
public class AdminRestController {

    private final AdminService adminService;

    @PostMapping("/reservations")
    public ResponseEntity<AdminReservationResponse> createReservation(
            @RequestBody final AdminReservationRequest adminReservationRequest
    ) {
        final AdminReservationResponse adminReservationResponse = adminService.saveByAdmin(adminReservationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(adminReservationResponse);
    }

    @GetMapping("/searchable-reservations")
    public ResponseEntity<List<AdminReservationResponse>> getReservationsBySearch(
            @ModelAttribute ReservationSearchRequest searchRequest
    ) {
        final List<AdminReservationResponse> searchedResponses = adminService.findByInFromTo(searchRequest);

        return ResponseEntity.ok(searchedResponses);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<ReservationWaitingResponse>> waitingManagement(
    ) {
        final List<ReservationWaitingResponse> waitingResponses = adminService.findAllWaitingReservations();

        return ResponseEntity.ok(waitingResponses);
    }

    @DeleteMapping("/waiting-deny/{id}")
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable final Long id
    ) {
        adminService.deleteWaitingById(id);

        return ResponseEntity.noContent().build();
    }
}
