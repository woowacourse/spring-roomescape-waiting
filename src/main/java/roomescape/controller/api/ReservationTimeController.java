package roomescape.controller.api;

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
import roomescape.dto.time.AvailableReservationTimeResponseDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponseDto>> getAllReservationTimes() {
        List<ReservationTimeResponseDto> allReservationTimeResponses = reservationTimeService.findAllReservationTimes();
        return ResponseEntity.ok(allReservationTimeResponses);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponseDto>> getAvailableReservationTimes(@RequestParam("date") LocalDate date, @RequestParam("themeId") Long themeId) {
        List<AvailableReservationTimeResponseDto> allReservationTimeResponses = reservationTimeService.findAvailableReservationTimes(date, themeId);
        return ResponseEntity.ok(allReservationTimeResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponseDto> addReservationTime(@RequestBody final ReservationTimeCreateRequestDto requestDto) {
        ReservationTimeResponseDto responseDto = reservationTimeService.createReservationTime(requestDto);
        return ResponseEntity.created(URI.create("times/" + responseDto.id())).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }
}
