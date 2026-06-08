package roomescape.theme.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Theme {
    private final Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
}
