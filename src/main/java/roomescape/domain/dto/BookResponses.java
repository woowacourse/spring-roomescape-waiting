package roomescape.domain.dto;

import java.util.List;

public class BookResponses {
    private final List<BookResponse> data;

    public BookResponses(final List<BookResponse> data) {
        this.data = data;
    }

    public List<BookResponse> getData() {
        return data;
    }
}
