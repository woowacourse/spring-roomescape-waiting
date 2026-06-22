package roomescape.theme.domain;

import roomescape.common.exception.RoomEscapeException;
import roomescape.theme.exception.ThemeErrorCode;

public class Theme {

    private static final long DEFAULT_RUNNING_TIME = 60L;
    private static final long NAME_MAX_LENGTH = 20L;
    private static final long DESCRIPTION_MIN_LENGTH = 3L;

    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final Long runningTime;

    private Theme(Long id, String name, String description, String imageUrl, Long runningTime) {
        validateName(name);
        validateDescription(description);
        validateImageUrl(imageUrl);
        validateRunningTime(runningTime);
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.runningTime = runningTime;
    }

    public static Theme create(String name, String description, String imageUrl) {
        return new Theme(null, name, description, imageUrl, DEFAULT_RUNNING_TIME);
    }

    public static Theme of(Long id, String name, String description, String imageUrl, Long runningTime) {
        validateId(id);
        return new Theme(id, name, description, imageUrl, runningTime);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new IllegalStateException("ID는 필수값입니다.");
        }
        if (id < 1) {
            throw new IllegalStateException("ID는 1 이상의 숫자여야 합니다. (입력값: " + id + ")");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > NAME_MAX_LENGTH) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_INVALID_NAME);
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()
                || description.length() < DESCRIPTION_MIN_LENGTH) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_INVALID_DESCRIPTION);
        }
    }

    private static void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || !imageUrl.contains(".")) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_INVALID_URL);
        }
    }

    private static void validateRunningTime(Long runningTime) {
        if (runningTime == null || runningTime < 1) {
            throw new IllegalStateException("소요 시간은 1분 이상이어야 합니다. (입력값: " + runningTime + ")");
        }
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

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getRunningTime() {
        return runningTime;
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
