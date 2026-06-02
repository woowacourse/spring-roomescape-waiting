package roomescape.application.dto.command;

import roomescape.exception.client.InvalidCommandException;

public class ThemeCreateCommand {
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public ThemeCreateCommand(String name, String description, String thumbnailUrl) {
        validate(name, description, thumbnailUrl);
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    private void validate(String name, String description, String thumbnailUrl) {
        if (name == null || name.isBlank()) {
            throw new InvalidCommandException("테마 이름은 비어 있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidCommandException("테마 설명은 비어 있을 수 없습니다.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new InvalidCommandException("테마 썸네일은 비어 있을 수 없습니다.");
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnailUrl;
    }
}
