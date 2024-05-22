package roomescape.theme.dto.response;

import roomescape.theme.model.Theme;

public record FindPopularThemesResponse(Long id,
                                        String name,
                                        String description,
                                        String thumbnail) {
    public static FindPopularThemesResponse from(final Theme theme) {
        return new FindPopularThemesResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());
    }
}
