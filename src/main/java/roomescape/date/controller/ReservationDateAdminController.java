package roomescape.date.controller;

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
import roomescape.date.controller.dto.request.ReservationDateSaveDto;
import roomescape.date.controller.dto.request.ReservationDateStatusUpdateDto;
import roomescape.date.controller.dto.response.ReservationDateDetailDto;
import roomescape.date.domain.ReservationDate;
import roomescape.date.service.ReservationDateService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReservationDateAdminController {

    private final ReservationDateService reservationDateService;

    @AuthGuard(roles = MANAGER)
    @GetMapping("/dates")
    public ResponseEntity<List<ReservationDateDetailDto>> getReservationDates() {
        List<ReservationDateDetailDto> responseData = reservationDateService.readDates().stream()
            .map(ReservationDateDetailDto::from)
            .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PostMapping("/dates")
    public ResponseEntity<ReservationDateDetailDto> create(
        @Validated @RequestBody ReservationDateSaveDto dto
    ) {
        ReservationDate reservationDate = reservationDateService.register(dto.date());
        ReservationDateDetailDto responseData = ReservationDateDetailDto.from(reservationDate);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/dates/{id}/status")
    public ResponseEntity<ReservationDateDetailDto> updateStatus(
        @PathVariable Long id, @Validated @RequestBody ReservationDateStatusUpdateDto dto) {
        ReservationDate reservationDate = reservationDateService.updateStatus(id, dto.isActive());
        ReservationDateDetailDto responseData = ReservationDateDetailDto.from(reservationDate);
        return ResponseEntity.ok(responseData);
    }

}
