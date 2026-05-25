package roomescape.reservation.service.dto;

import roomescape.reservation.repository.PopularThemeQueryResult;

import java.util.List;

public record PopularThemesResult(List<PopularThemeQueryResult> popularThemes) {
}
