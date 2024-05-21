package roomescape.service.dto.output;

import java.util.List;

import roomescape.domain.reservation.Theme;

public record ThemeOutput(long id, String name, String description, String thumbnail) {

    public static ThemeOutput toOutput(final Theme theme) {
        return new ThemeOutput(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailAsString());
    }

    public static List<ThemeOutput> toOutputs(final List<Theme> themes) {
        return themes.stream()
                .map(ThemeOutput::toOutput)
                .toList();
    }
}
