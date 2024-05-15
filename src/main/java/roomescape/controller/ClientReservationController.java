package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.domain.dto.BookResponses;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.service.BookService;
import roomescape.service.ReservationService;

@RestController
public class ClientReservationController {
    private final BookService bookService;
    private final ReservationService reservationService;

    public ClientReservationController(final BookService bookService, final ReservationService reservationService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
    }

    @GetMapping("/books/{date}/{theme_id}")
    public ResponseEntity<BookResponses> read(@PathVariable(value = "date") LocalDate date,
                                              @PathVariable(value = "theme_id") Long themeId) {
        return ResponseEntity.ok(bookService.findAvailableBookList(date, themeId));
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
        return ResponseEntity.ok(reservationsMineResponses);
    }
}
