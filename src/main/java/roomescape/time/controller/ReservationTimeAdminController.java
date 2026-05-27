package roomescape.time.controller;

import static roomescape.member.domain.Role.MANAGER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.time.controller.dto.request.ReservationTimeSaveDto;
import roomescape.time.controller.dto.request.ReservationTimeStatusUpdateDto;
import roomescape.time.controller.dto.response.ReservationTimeDetailDto;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReservationTimeAdminController {

    private final ReservationTimeService reservationTimeService;

    @AuthGuard(roles = MANAGER)
    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeDetailDto>> getReservationTimes() {
        List<ReservationTimeDetailDto> responseData = reservationTimeService.readAll().stream()
            .map(ReservationTimeDetailDto::from)
            .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PostMapping("/times")
    public ResponseEntity<ReservationTimeDetailDto> create(
        @Validated @RequestBody ReservationTimeSaveDto reservationTimeSaveDto
    ) {
        ReservationTime reservationTime = reservationTimeService.register(reservationTimeSaveDto);
        ReservationTimeDetailDto responseData = ReservationTimeDetailDto.from(reservationTime);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/times/{id}/status")
    public ResponseEntity<ReservationTimeDetailDto> updateStatus(
        @PathVariable Long id, @Validated @RequestBody ReservationTimeStatusUpdateDto dto
    ) {
        ReservationTime reservationTime = reservationTimeService.updateStatus(id, dto.isActive());
        ReservationTimeDetailDto responseData = ReservationTimeDetailDto.from(reservationTime);
        return ResponseEntity.ok(responseData);
    }

}
