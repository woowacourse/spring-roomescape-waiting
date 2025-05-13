package roomescape.controller.time;

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
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.dto.reservationtime.AddReservationTimeDto;
import roomescape.dto.reservationtime.ReservationTimeResponseDto;
import roomescape.service.reservationtime.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponseDto>> allReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.allReservationTimes();
        List<ReservationTimeResponseDto> reservationTimeResponseDtos = reservationTimes.stream()
                .map((reservationTime) -> new ReservationTimeResponseDto(reservationTime.getId(),
                        reservationTime.getTime()))
                .toList();

        return ResponseEntity.ok(reservationTimeResponseDtos);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponseDto> addReservationTime(
            @RequestBody @Valid AddReservationTimeDto newReservationTimeDto) {
        long id = reservationTimeService.addReservationTime(newReservationTimeDto);
        ReservationTime reservationTime = reservationTimeService.getReservationTimeById(id);
        ReservationTimeResponseDto reservationTimeResponseDto = new ReservationTimeResponseDto(reservationTime.getId(),
                reservationTime.getTime());
        return ResponseEntity.created(URI.create("/times/")).body(reservationTimeResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservations(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
