package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.dto.BookResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookServiceTest {
    private final BookService bookService;

    @Autowired
    public BookServiceTest(final BookService bookService) {
        this.bookService = bookService;
    }

    @DisplayName("예약 가능한 시간 목록들을 반환한다.")
    @Test
    void given_when_findAvailableBookList_thenReturnBookResponse() {
        //when
        final List<BookResponse> bookResponses = bookService.findAvailableBookList(LocalDate.parse("2099-05-08"), 1L)
                .getData();
        //then
        assertThat(bookResponses.size()).isEqualTo(4);
    }
}
