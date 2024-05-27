package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.domain.dto.BookResponse;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;

import java.net.URI;
import java.time.LocalDate;

@RestController
public class ClientReservationController {
    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;

    public ClientReservationController(final ReservationTimeService reservationTimeService,
                                       final ReservationService reservationService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationService = reservationService;
    }

    @GetMapping("/books/{date}/{theme_id}")
    public ResponseEntity<ResponsesWrapper<BookResponse>> getAvailableBooks(@PathVariable(value = "date") LocalDate date,
                                                                            @PathVariable(value = "theme_id") Long themeId) {
        return ResponseEntity.ok(reservationTimeService.findAvailableBookList(date, themeId));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> register(@RequestBody ReservationRequest reservationRequest,
                                                        Member member) {
        final ReservationResponse reservationResponse =
                reservationService.register(reservationRequest.with(member.getId()));
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<ResponsesWrapper<ReservationsMineResponse>> getMemberReservations(Member member) {
        return ResponseEntity.ok(reservationService.findMemberReservations(member));
    }

    @DeleteMapping("/reservations-mine/wait/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable(value = "id") Long id, Member member) {
        reservationService.deleteByIdWithWaiting(id, member);
        return ResponseEntity.noContent().build();
    }
}
