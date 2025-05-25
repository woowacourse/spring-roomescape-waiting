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
import roomescape.global.auth.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.service.ReservationTimeModuleService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeModuleService reservationTimeModuleService;

    public ReservationTimeController(final ReservationTimeModuleService reservationTimeModuleService) {
        this.reservationTimeModuleService = reservationTimeModuleService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        return ResponseEntity.ok(reservationTimeModuleService.getReservationTimes());
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getAvailableReservationTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId
    ) {
        return ResponseEntity.ok(reservationTimeModuleService.getAvailableReservationTimes(date, themeId));
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @RequestBody ReservationTimeCreateRequest request
    ) {
        ReservationTimeResponse dto = reservationTimeModuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTimes(
            @PathVariable("id") Long id
    ) {
        reservationTimeModuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
