package roomescape.adapter.web.dto.response;

import roomescape.application.dto.result.PopularThemeResult;

public class PopularThemeResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;
    private final long reservationCount;

    public PopularThemeResponse(
            Long id,
            String name,
            String description,
            String thumbnailUrl,
            long reservationCount
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.reservationCount = reservationCount;
    }

    public static PopularThemeResponse from(PopularThemeResult result) {
        return new PopularThemeResponse(
                result.getTheme().getId(),
                result.getTheme().getName(),
                result.getTheme().getDescription(),
                result.getTheme().getThumbnailUrl(),
                result.getReservationCount()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public long getReservationCount() {
        return reservationCount;
    }
}
