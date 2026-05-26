package roomescape.dto.theme;

public record ThemeReservationTimeResponse(
        Long id,
        String startAt,
        boolean isReserved
) {
}
