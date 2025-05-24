package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationCommandService;
import roomescape.application.ReservationQueryService;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.ReservationDto;
import roomescape.application.dto.ReservationWaitingDto;
import roomescape.application.dto.UserReservationCreateDto;
import roomescape.application.dto.UserWaitingCreateDto;
import roomescape.infrastructure.AuthenticatedMemberId;
import roomescape.presentation.controller.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationCommandService commandService;
    private final ReservationQueryService queryService;

    public ReservationController(ReservationQueryService queryService, ReservationCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(
            @Valid @RequestBody UserReservationCreateDto request,
            @AuthenticatedMemberId MemberIdDto memberIdDto
    ) {
        ReservationDto reservationDto = commandService.registerReservationByUser(request, memberIdDto.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationDto);
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationDto> createWaiting(
            @Valid @RequestBody UserWaitingCreateDto request,
            @AuthenticatedMemberId MemberIdDto memberIdDto
    ) {
        ReservationDto reservationDto = commandService.registerWaitingByUser(request, memberIdDto.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationDto);
    }

    @GetMapping
    public List<ReservationDto> getAllReservations() {
        return queryService.getAllReservations();
    }

    @GetMapping("/member")
    public List<ReservationWaitingResponse> getMemberReservations(@AuthenticatedMemberId MemberIdDto memberIdDto) {
        List<ReservationWaitingDto> reservationWaitingDtos = queryService.getReservationsByMember(memberIdDto.id());
        return reservationWaitingDtos.stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        commandService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
