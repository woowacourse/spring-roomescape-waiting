package roomescape.domain.dto;

import java.util.List;

public class ResponsesWrapper<T> {
    private final List<T> data;

    public ResponsesWrapper(final List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }
}
