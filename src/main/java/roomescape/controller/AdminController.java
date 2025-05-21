package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.business.ReservationCreationContent;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservationByAdmin(
            @RequestBody AdminReservationRequest request
    ) {
        ReservationCreationContent creationRequest = new ReservationCreationContent(request);
        ReservationResponse reservationResponse =
                reservationService.addReservation(request.memberId(), creationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
    }

    @GetMapping("/search")
    public List<ReservationResponse> searchReservationsByFilter(
            @RequestParam("memberId") Long memberId,
            @RequestParam("themeId") Long themeId,
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to
    ) {
        return reservationService.findReservationsByFilter(memberId, themeId, from, to);
    }
}
