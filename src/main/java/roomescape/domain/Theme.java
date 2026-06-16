package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.domain.exception.InvalidInputException;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;

    protected Theme() {
    }

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("테마 이름은 비어있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidInputException("테마 설명은 비어있을 수 없습니다.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new InvalidInputException("테마 썸네일 URL은 비어있을 수 없습니다.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
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
}
