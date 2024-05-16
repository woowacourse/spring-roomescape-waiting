package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.BookResponse;
import roomescape.service.BookService;

@RestController
@RequestMapping("/books")
public class BookingController {

    private final BookService bookService;

    public BookingController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> read(@RequestParam LocalDate date,
                                                   @RequestParam Long themeId) {
        List<BookResponse> books = bookService.findAvaliableBookList(date, themeId);
        return ResponseEntity.ok(books);
    }
}
