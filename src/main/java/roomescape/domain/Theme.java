package roomescape.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Theme {
    private final Long id;
    private final String name;
    private final String thumbnailUrl;
    private final String description;

    public static Theme create(long id, String name, String thumbnailUrl, String description) {
        return new Theme(id, name, thumbnailUrl, description);
    }

    public long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String thumbnailUrl() {
        return thumbnailUrl;
    }

    public String description() {
        return description;
    }
}
