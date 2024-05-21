package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.domain.WaitingWithRank;
import roomescape.domain.dto.BookResponses;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.WaitingService;

@RestController
public class ClientReservationController {
    private final ReservationTimeService reservationTimeService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ClientReservationController(final ReservationTimeService reservationTimeService,
                                       final ReservationService reservationService,
                                       final WaitingService waitingService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/books/{date}/{theme_id}")
    public ResponseEntity<BookResponses> read(@PathVariable(value = "date") LocalDate date,
                                              @PathVariable(value = "theme_id") Long themeId) {
        return ResponseEntity.ok(reservationTimeService.findAvailableBookList(date, themeId));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody ReservationRequest reservationRequest,
                                                      Member member) {
        final ReservationResponse reservationResponse =
                reservationService.create(reservationRequest.with(member.getId()));
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationsMineResponse>> readByMember(Member member) {
        final List<ReservationsMineResponse> reservationsMineResponses =
                reservationService.findReservationsByMember(member);
        final List<WaitingWithRank> waitingsByMember = waitingService.findWaitingsByMember(member);
        List<ReservationsMineResponse> waitingResponses = waitingsByMember.stream()
                .map(ReservationsMineResponse::from)
                .toList();
        reservationsMineResponses.addAll(waitingResponses);
        return ResponseEntity.ok(reservationsMineResponses);
    }
}
