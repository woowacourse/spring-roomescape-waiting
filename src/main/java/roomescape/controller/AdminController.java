package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ReservationService reservationService;

    public AdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String mainPage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String reservationPage() {
        return "admin/reservation-new";
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(@RequestBody ReservationRequest reservationRequest) {
        ReservationResponse saved = reservationService.save(reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + saved.id()))
                .body(saved);
    }

    @GetMapping("/reservations")
    @ResponseBody
    public List<ReservationResponse> search(@RequestParam long memberId,
                                            @RequestParam long themeId,
                                            @RequestParam LocalDate start,
                                            @RequestParam LocalDate end) {
        return reservationService.findByMemberAndThemeBetweenDates(memberId, themeId, start, end);
    }

    @GetMapping("/time")
    public String reservationTimePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String themePage() {
        return "admin/theme";
    }
}
