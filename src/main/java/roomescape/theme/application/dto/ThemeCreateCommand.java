package roomescape.theme.application.dto;

import roomescape.theme.domain.Theme;

public record ThemeCreateCommand(String name, String description, String thumbnailImgUrl, Long price) {

    public Theme toEntity() {
        return Theme.builder()
                .name(name)
                .description(description)
                .thumbnailImgUrl(thumbnailImgUrl)
                .price(price)
                .build();
    }
}
