package roomescape.domain.dto;

import java.util.List;

public class ThemeResponses {
    private final List<ThemeResponse> data;

    public ThemeResponses(final List<ThemeResponse> data) {
        this.data = data;
    }

    public List<ThemeResponse> getData() {
        return data;
    }
}
