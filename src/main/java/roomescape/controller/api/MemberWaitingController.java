package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.waiting.MemberWaitingCreateRequestDto;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.service.dto.WaitingCreateDto;
import roomescape.service.waiting.WaitingCommandService;

@RestController
public class MemberWaitingController {

    private final WaitingCommandService commandService;

    public MemberWaitingController(WaitingCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingResponseDto> addWaiting(@CurrentMember LoginInfo info,
                                                         @RequestBody final MemberWaitingCreateRequestDto dto){
        WaitingCreateDto createDto = new WaitingCreateDto(dto.date(), info.id(), dto.themeId(), dto.timeId());
        WaitingResponseDto waitingResponseDto = commandService.registerWaiting(createDto);
        return ResponseEntity.ok(waitingResponseDto);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") final Long id){
        commandService.cancelWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
