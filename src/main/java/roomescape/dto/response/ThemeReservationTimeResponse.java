package roomescape.dto.response;

public record ThemeReservationTimeResponse(
        Long id,
        String startAt,
        boolean isReserved
) {
}
