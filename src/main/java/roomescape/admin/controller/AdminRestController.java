package roomescape.admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.AdminReservationResponse;
import roomescape.admin.dto.ReservationSearchRequest;
import roomescape.admin.service.AdminService;
import roomescape.reservation.domain.Reservation;

@RequestMapping("/admin")
@RequiredArgsConstructor
@RestController
public class AdminRestController {

    private final AdminService adminService;

    @PostMapping("/reservations")
    public ResponseEntity<AdminReservationResponse> createReservation(
            @RequestBody final AdminReservationRequest adminReservationRequest
    ) {
        final Long id = adminService.saveByAdmin(
                adminReservationRequest.date(),
                adminReservationRequest.themeId(),
                adminReservationRequest.timeId(),
                adminReservationRequest.memberId()
        );
        final Reservation found = adminService.getById(id);

        return ResponseEntity.status(HttpStatus.CREATED).body(AdminReservationResponse.from(found));
    }

    @GetMapping("/searchable-reservations")
    public ResponseEntity<List<AdminReservationResponse>> getReservationsBySearch(
            @ModelAttribute ReservationSearchRequest searchRequest
    ) {
        final List<Reservation> searchedReservations = adminService.findByInFromTo(
                searchRequest.themeId(),
                searchRequest.memberId(),
                searchRequest.dateFrom(),
                searchRequest.dateTo()
        );

        final List<AdminReservationResponse> searchedResponses = searchedReservations.stream()
                .map(AdminReservationResponse::from)
                .toList();

        return ResponseEntity.ok(searchedResponses);
    }
}
