package roomescape.slot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.slot.controller.dto.response.SlotDetailDto;
import roomescape.slot.service.ReservationSlotService;

import java.util.List;

import static roomescape.member.domain.Role.MANAGER;
import static roomescape.member.domain.Role.MEMBER;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ReservationSlotController {

    private final ReservationSlotService reservationSlotService;

    @AuthGuard(roles = {MEMBER, MANAGER})
    @GetMapping("/slots")
    public ResponseEntity<List<SlotDetailDto>> get() {
        List<SlotDetailDto> responseData = reservationSlotService.findAllSlot().stream()
                .map(SlotDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

}
