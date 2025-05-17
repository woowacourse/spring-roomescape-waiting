package roomescape.theme.service.converter;

import java.util.List;
import roomescape.theme.controller.dto.CreateThemeWebRequest;
import roomescape.theme.controller.dto.ThemeWebResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.theme.service.dto.CreateThemeServiceRequest;

public class ThemeConverter {

    public static Theme toDomain(final CreateThemeServiceRequest createThemeServiceRequest) {
        return Theme.withoutId(
                ThemeName.from(createThemeServiceRequest.name()),
                ThemeDescription.from(createThemeServiceRequest.description()),
                ThemeThumbnail.from(createThemeServiceRequest.thumbnail())
        );
    }

    public static ThemeWebResponse toDto(final Theme theme) {
        return new ThemeWebResponse(
                theme.getId(),
                theme.getName().getValue(),
                theme.getDescription().getValue(),
                theme.getThumbnail().getValue());
    }

    public static List<ThemeWebResponse> toDto(final List<Theme> themes) {
        return themes.stream()
                .map(ThemeConverter::toDto)
                .toList();
    }

    public static CreateThemeServiceRequest toServiceDto(final CreateThemeWebRequest createThemeWebRequest) {
        return new CreateThemeServiceRequest(
                createThemeWebRequest.name(),
                createThemeWebRequest.description(),
                createThemeWebRequest.thumbnail()
        );
    }
}
