package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.service.WaitingService;
import roomescape.service.dto.ReservationDto;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping // TODO: 얘도 관리자 권한이 필요하지 않을까?
    public ResponseEntity<List<WaitingResponse>> getAllWaiting() {
        List<Waiting> allWaiting = waitingService.findAllWaiting();
        List<WaitingResponse> response = allWaiting.stream()
                .map(WaitingResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> addWaiting(@Valid @RequestBody ReservationRequest request, LoginMember member) {
        ReservationDto reservationDto = ReservationDto.of(request, member); // TODO: DTO 분리?
        Waiting waiting = waitingService.saveWaiting(reservationDto);
        WaitingResponse response = WaitingResponse.from(waiting); // TODO: essential?
        return ResponseEntity
                .created(URI.create("/reservations/waiting/" + waiting.getId()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@NotNull @Min(1) @PathVariable("id") Long id, LoginMember member) {
        waitingService.deleteWaitingOfMember(id, member);
        return ResponseEntity.noContent().build();
    }
}
