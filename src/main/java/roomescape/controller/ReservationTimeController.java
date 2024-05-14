package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.service.AvailableTimeService;
import roomescape.service.ReservationTimeService;

//Todo 일부 기능 관리자만 접근할 수 있도록 깔끔하게 하는 방법 생각해보기
@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;
    private final AvailableTimeService availableTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService,
                                     AvailableTimeService availableTimeService) {
        this.reservationTimeService = reservationTimeService;
        this.availableTimeService = availableTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> save(@RequestBody ReservationTimeRequest reservationTimeRequest) {
        ReservationTimeResponse saved = reservationTimeService.save(reservationTimeRequest);
        return ResponseEntity.created(URI.create("/times/" + saved.id()))
                .body(saved);
    }

    @GetMapping
    public List<ReservationTimeResponse> findAll() {
        return reservationTimeService.findAll();
    }

    @GetMapping("/book-able")
    public List<AvailableTimeResponse> findByThemeAndDate(@RequestParam LocalDate date, @RequestParam long themeId) {
        return availableTimeService.findByThemeAndDate(date, themeId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
