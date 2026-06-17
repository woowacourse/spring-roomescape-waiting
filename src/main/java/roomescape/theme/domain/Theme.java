package roomescape.theme.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(of = {"name", "description"})
public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailImgUrl;
    private final Long price;

    public Theme withId(Long generatedId) {
        return Theme.builder()
                .id(generatedId)
                .name(this.name)
                .description(this.description)
                .thumbnailImgUrl(this.thumbnailImgUrl)
                .price(this.price)
                .build();
    }
}
