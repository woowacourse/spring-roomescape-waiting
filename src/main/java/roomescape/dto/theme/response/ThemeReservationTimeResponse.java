package roomescape.dto.theme.response;

public record ThemeReservationTimeResponse(
        Long id,
        String startAt,
        boolean isReserved
) {
}
