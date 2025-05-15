package roomescape.reservationtime.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.domain.dto.AvailableReservationTimeResponseDto;
import roomescape.reservationtime.domain.dto.ReservationTimeRequestDto;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.user.domain.User;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService service;

    public ReservationTimeController(ReservationTimeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponseDto>> findAll() {
        List<ReservationTimeResponseDto> resDtos = service.findAll();
        return ResponseEntity.ok(resDtos);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<AvailableReservationTimeResponseDto>> findReservationTimesWithAvailableStatus(
            @RequestParam("themeId") Long themeId, @RequestParam("date") LocalDate date, User user) {
        List<AvailableReservationTimeResponseDto> availableReservationTimeResponseDtos = service.findReservationTimesWithAvailableStatus(
                themeId,
                date,
                user);
        return ResponseEntity.ok(availableReservationTimeResponseDtos);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponseDto> add(@RequestBody ReservationTimeRequestDto requestDto) {
        ReservationTimeResponseDto resDto = service.add(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> deleteById(@PathVariable("reservationTimeId") Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
