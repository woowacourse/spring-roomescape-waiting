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
import roomescape.application.ReservationService;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.ReservationDto;
import roomescape.application.dto.ReservationWaitingDto;
import roomescape.application.dto.UserReservationCreateDto;
import roomescape.infrastructure.AuthenticatedMemberId;
import roomescape.presentation.controller.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReservationDto> getAllReservations() {
        return service.getAllReservations();
    }

    @GetMapping("/member")
    public List<ReservationWaitingResponse> getMemberReservations(@AuthenticatedMemberId MemberIdDto memberIdDto) {
        List<ReservationWaitingDto> reservationWaitingDtos = service.getReservationsByMember(memberIdDto.id());
        return reservationWaitingDtos.stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(
            @Valid @RequestBody UserReservationCreateDto request,
            @AuthenticatedMemberId MemberIdDto memberIdDto
    ) {
        ReservationDto reservationDto = service.registerReservationByUser(request, memberIdDto.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
