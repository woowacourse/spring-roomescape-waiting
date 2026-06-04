package roomescape.service.dto.result;

import roomescape.domain.slot.theme.Theme;

public record ThemeResult(
        Long id,
        String name,
        String description,
        String url
) {

    public static ThemeResult from(Theme theme) {
        return new ThemeResult(
                theme.getId(),
                theme.getName().value(),
                theme.getDescription().value(),
                theme.getUrl().value()
        );
    }
}
