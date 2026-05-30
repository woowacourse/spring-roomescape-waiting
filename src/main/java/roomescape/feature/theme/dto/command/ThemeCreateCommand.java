package roomescape.feature.theme.dto.command;

import roomescape.feature.theme.domain.ThemeDescription;
import roomescape.feature.theme.domain.ThemeImageUrl;
import roomescape.feature.theme.domain.ThemeName;

public record ThemeCreateCommand(
    ThemeName name,
    ThemeDescription description,
    ThemeImageUrl imageUrl
) {
}
