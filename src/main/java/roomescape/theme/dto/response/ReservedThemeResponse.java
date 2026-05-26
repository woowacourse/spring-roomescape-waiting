package roomescape.theme.dto.response;

public record ReservedThemeResponse(
        long id,
        String name,
        String description,
        String imageUrl,
        int reservationCount
) {
}
