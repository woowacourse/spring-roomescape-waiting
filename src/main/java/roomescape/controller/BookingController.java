package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.BookResponse;
import roomescape.service.BookService;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/books")
public class BookingController {
    private final BookService bookService;
    private final ReservationService reservationService;

    public BookingController(final BookService bookService, final ReservationService reservationService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
    }

    @GetMapping("/{date}/{theme_id}")
    public ResponseEntity<List<BookResponse>> read(@PathVariable final LocalDate date,
                                                   @PathVariable(value = "theme_id") final Long themeId) {
        List<BookResponse> books = bookService.findAvaliableBookList(date, themeId);
        return ResponseEntity.ok(books);
    }
}
