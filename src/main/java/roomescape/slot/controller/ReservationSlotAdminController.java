package roomescape.slot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.slot.controller.dto.request.SlotSaveDto;
import roomescape.slot.controller.dto.response.SlotDetailDto;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.service.ReservationSlotService;

import static roomescape.member.domain.Role.MANAGER;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReservationSlotAdminController {

    private final ReservationSlotService slotService;

    @AuthGuard(roles = MANAGER)
    @PostMapping("/slots")
    public ResponseEntity<SlotDetailDto> create(@RequestBody SlotSaveDto dto) {
        ReservationSlot slot = slotService.save(dto.toCommand());
        SlotDetailDto responseData = SlotDetailDto.from(slot);
        return ResponseEntity.ok(responseData);
    }

}
