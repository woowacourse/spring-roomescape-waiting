package roomescape.reservation.service.dto;

import roomescape.reservation.query.dto.PopularThemeQueryResult;

import java.util.List;

public record PopularThemesResult(List<PopularThemeQueryResult> popularThemes) {
}
