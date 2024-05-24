package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationService;
import roomescape.service.request.ReservationSaveDto;
import roomescape.service.response.ReservationDto;
import roomescape.web.auth.Auth;
import roomescape.web.controller.request.LoginMember;
import roomescape.web.controller.request.MemberReservationRequest;
import roomescape.web.controller.response.MemberReservationResponse;
import roomescape.web.controller.response.ReservationMineResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<MemberReservationResponse> reserve(@Valid @RequestBody MemberReservationRequest request,
                                                             @Valid @Auth
                                                             LoginMember loginMember) {
        ReservationDto appResponse = reservationService.save(
                new ReservationSaveDto(request.date(), request.timeId(),
                        request.themeId(), loginMember.id()));

        Long id = appResponse.id();
        MemberReservationResponse memberReservationResponse = MemberReservationResponse.from(appResponse);

        return ResponseEntity.created(URI.create("/reservations/" + id))
                .body(memberReservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MemberReservationResponse>> getReservations() {
        List<ReservationDto> appResponses = reservationService.findAll();
        List<MemberReservationResponse> memberReservationResponse = appResponses.stream()
                .map(MemberReservationResponse::from)
                .toList();

        return ResponseEntity.ok(memberReservationResponse);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationMineResponse>> getMyReservations(@Auth LoginMember loginMember) {
        List<ReservationMineResponse> reservationMineRespons = reservationService.findByMemberId(loginMember.id())
                .stream()
                .map(ReservationMineResponse::new)
                .toList();

        return ResponseEntity.ok(reservationMineRespons);
    }

}
