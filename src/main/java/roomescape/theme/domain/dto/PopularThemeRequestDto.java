package roomescape.theme.domain.dto;

public final class PopularThemeRequestDto {

    private final int size;
    private final String sortDirection;

    public PopularThemeRequestDto(
            int size,
            String sortDirection
    ) {
        this.size = size;
        this.sortDirection = sortDirection;
    }

    public PopularThemeRequestDto() {
        this(10, "DESC");
    }

    public int size() {
        return size;
    }

    public String sortDirection() {
        return sortDirection;
    }
}
