package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.Member;
import roomescape.domain.dto.BookResponses;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.service.BookService;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;

@RestController
public class ClientReservationController {
    private final BookService bookService;
    private final ReservationService reservationService;

    public ClientReservationController(final BookService bookService, final ReservationService reservationService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
    }

    @GetMapping("/books/{date}/{theme_id}")
    public ResponseEntity<BookResponses> read(@PathVariable(value = "date") LocalDate date, @PathVariable(value = "theme_id") Long themeId) {
        return ResponseEntity.ok(bookService.findAvaliableBookList(date, themeId));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody ReservationRequest reservationRequest, Member member) {
        final ReservationResponse reservationResponse = reservationService.create(reservationRequest.with(member.getId()));
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }
}
