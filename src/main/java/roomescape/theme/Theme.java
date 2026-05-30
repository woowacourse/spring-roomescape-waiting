package roomescape.theme;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;
}
