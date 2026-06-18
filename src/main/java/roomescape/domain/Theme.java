package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String url;

    public Theme() {
    }

    public Theme(String name, String description, String url) {
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
