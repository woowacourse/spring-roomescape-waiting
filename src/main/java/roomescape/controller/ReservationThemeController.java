package roomescape.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.dto.ReservationThemeRequest;
import roomescape.service.dto.ReservationThemeResponse;
import roomescape.service.ReservationThemeService;

@RestController
@RequestMapping("/themes")
public class ReservationThemeController {

    private final ReservationThemeService reservationThemeService;

    public ReservationThemeController(final ReservationThemeService reservationThemeService) {
        this.reservationThemeService = reservationThemeService;
    }

    @GetMapping()
    public ResponseEntity<List<ReservationThemeResponse>> reservationThemeList() {
        return ResponseEntity.status(HttpStatus.OK).body(reservationThemeService.findReservationThemes());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ReservationThemeResponse>> reservationThemeRankingList() {
        return ResponseEntity.status(HttpStatus.OK).body(reservationThemeService.findPopularThemes());
    }

    @PostMapping()
    public ResponseEntity<ReservationThemeResponse> reservationThemeAdd(
            @RequestBody ReservationThemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationThemeService.addReservationTheme(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> reservationThemeRemove(@PathVariable(name = "id") long id) {
        reservationThemeService.removeReservationTheme(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
