package roomescape.domain.theme.dto;

import roomescape.domain.theme.domain.Theme;

public record ThemeRankingResponse(Long id, String name, String description, String thumbnail) {

    private static final String DEFAULT_THUMBNAIL_URL = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";

    public static ThemeRankingResponse from(Theme theme) {
        String thumbnail = theme.getThumbnail();
        if (thumbnail == null) {
            thumbnail = DEFAULT_THUMBNAIL_URL;
        }
        return new ThemeRankingResponse(theme.getId(), theme.getName(), theme.getDescription(), thumbnail);
    }
}
