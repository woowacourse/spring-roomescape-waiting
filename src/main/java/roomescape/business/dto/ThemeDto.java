package roomescape.business.dto;

import org.springframework.jdbc.core.RowMapper;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ThemeName;

public record ThemeDto(
        Id id,
        ThemeName name,
        String description,
        String thumbnail
) {
    public static ThemeDto fromEntity(final Theme theme) {
        return new ThemeDto(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    public static RowMapper<ThemeDto> ROW_MAPPER = (rs, rowNum) -> new ThemeDto(
            Id.create(rs.getString("id")),
            new ThemeName(rs.getString("theme_name")),
            rs.getString("description"),
            rs.getString("thumbnail")
    );

}
