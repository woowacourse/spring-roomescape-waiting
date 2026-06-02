package roomescape.domain;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String url;

    public Theme(Long id, String name, String description, String url) {
        this.id = id;
        validateName(name);
        validateDescription(description);
        validateUrl(url);
        this.name = name;
        this.description = description;
        this.url = url;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마명이 유효하지 않습니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("설명이 유효하지 않습니다.");
        }
    }

    private void validateUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("테마 사진 업로드에 실패했습니다.");
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

    public String getUrl() {
        return url;
    }
}
