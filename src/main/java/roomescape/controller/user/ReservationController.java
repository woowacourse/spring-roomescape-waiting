package roomescape.controller.user;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.auth.LoginMember;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationOrder;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.dto.response.PaymentReadyResponse;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<PaymentReadyResponse> create(
            @LoginMember Member member,
            @Valid @RequestBody ReservationRequestDto request
    ) {
        ReservationOrder result = reservationService.create(member, request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.reservation().getId())
                .toUri();
        return ResponseEntity.created(uri).body(PaymentReadyResponse.from(result.reservation(), result.order()));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> findMyReservations(@LoginMember Member member) {
        List<ReservationResponseDto> responses = reservationService.findAllByMemberId(member.getId()).stream()
                .map(ReservationResponseDto::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> update(
            @LoginMember Member member,
            @PathVariable Long id,
            @Valid @RequestBody ReservationPatchDto request
    ) {
        Reservation reservation = reservationService.updateByUser(id, member, request);
        return ResponseEntity.ok(ReservationResponseDto.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @LoginMember Member member,
            @PathVariable Long id
    ) {
        reservationService.cancel(id, member);
        return ResponseEntity.noContent().build();
    }
}
