package roomescape.reservation.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.service.ReservationCreateService;
import roomescape.reservation.service.ReservationFindService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationFindService findService;
    private final ReservationCreateService updateService;

    public AdminReservationController(ReservationFindService findService, ReservationCreateService updateService) {
        this.findService = findService;
        this.updateService = updateService;
    }

    @GetMapping
    public List<ReservationResponse> findReservations(
            @ModelAttribute ReservationSearchRequest searchRequest) {
        return findService.findReservations(searchRequest);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationCreateRequest request) {
        ReservationResponse response = updateService.createReservation(request);

        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location)
                .body(response);
    }
}
