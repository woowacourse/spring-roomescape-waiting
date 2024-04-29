package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.Member;
import roomescape.dto.MemberReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping//todo: 관리자와 관련된 것 같은 기능은 다 /admin 으로 변경하기
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> responses = reservationService.getAllReservations();

        return ResponseEntity.ok()
                .body(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody MemberReservationRequest request,
            Member member) {
        ReservationResponse response = reservationService.addMemberReservation(request, member);
        URI location = URI.create("/reservations/" + response.id());

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteById(id);

        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ReservationResponse>> getFilteredReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ReservationResponse> response = reservationService.getFilteredReservations(themeId, memberId, dateFrom, dateTo);

        return ResponseEntity.ok(response);
    }
}
