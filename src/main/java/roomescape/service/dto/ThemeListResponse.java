package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class ThemeListResponse {
    private final List<ThemeResponse> themes;

    @JsonCreator
    public ThemeListResponse(List<ThemeResponse> themes) {
        this.themes = themes;
    }

    public List<ThemeResponse> getThemes() {
        return themes;
    }
}
