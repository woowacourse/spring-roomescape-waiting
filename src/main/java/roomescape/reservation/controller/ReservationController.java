package roomescape.reservation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import roomescape.auth.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.BookingResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.service.ReservationService;

@Tag(name = "예약", description = "예약 생성·조회·수정·삭제 API")
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@LoginMember Member member) {
        return ResponseEntity.ok(reservationService.getReservationsByMemberId(member.getId()));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createReservation(@LoginMember Member member,
                                                             @Valid @RequestBody ReservationRequest request) {
        BookingResponse response = reservationService.createReservation(member, request);
        String location = "WAITING".equals(response.status())
                ? "/waitings/" + response.id()
                : "/reservations/" + response.id();
        return ResponseEntity.created(URI.create(location)).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@LoginMember Member member, @PathVariable Long id) {
        reservationService.deleteReservation(id, member.getId());
        return ResponseEntity.noContent().build();
    }
}
