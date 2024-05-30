package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.dto.request.MemberReservationRequest;
import roomescape.dto.request.ReservationDto;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationCreationService;
import roomescape.service.ReservationDeletionService;
import roomescape.service.ReservationQueryService;

import java.net.URI;
import java.util.List;

@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationCreationService reservationCreationService;
    private final ReservationQueryService reservationQueryService;
    private final ReservationDeletionService reservationDeletionService;

    public ReservationController(
            ReservationCreationService reservationCreationService,
            ReservationQueryService reservationQueryService,
            ReservationDeletionService reservationDeletionService) {
        this.reservationCreationService = reservationCreationService;
        this.reservationQueryService = reservationQueryService;
        this.reservationDeletionService = reservationDeletionService;
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<MyReservationResponse>> getMyReservations(Member member) {
        List<MyReservationResponse> reservations = reservationQueryService.getMyReservations(member);
        MultipleResponse<MyReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody MemberReservationRequest request, Member member) {
        ReservationDto reservationDto = ReservationDto.mapToApplicationDto(request, member.getId());
        ReservationResponse response = reservationCreationService.addReservation(reservationDto);

        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, Member member){
        reservationDeletionService.deleteById(id, member);

        return ResponseEntity.noContent()
                .build();
    }
}
