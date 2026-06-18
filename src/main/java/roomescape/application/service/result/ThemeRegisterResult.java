package roomescape.application.service.result;

import roomescape.domain.Theme;

public record ThemeRegisterResult(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        int price
) {
    public ThemeRegisterResult(long id, String name, String description, String thumbnailImageUrl) {
        this(id, name, description, thumbnailImageUrl, 0);
    }

    public static ThemeRegisterResult from(Theme theme){
        return new ThemeRegisterResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl(),
                theme.getPrice());
    }
}
