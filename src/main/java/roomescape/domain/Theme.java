package roomescape.domain;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;

    // JPA용
    protected Theme() {
    }

    // 사용자 생성
    public Theme(String name, String description, String thumbnailUrl) {
        validateNameLength(name);
        validateDescriptionLength(description);
        validateThumbnailUrlLength(thumbnailUrl);
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    // JDBC → JPA 과정에서 깨지는 것을 방지하기 위한 코드로 곧 삭제 예정
    public Theme(Long id, String name, String description, String thumbnailUrl) {
        validateNameLength(name);
        validateDescriptionLength(description);
        validateThumbnailUrlLength(thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }


    private void validateNameLength(String name) {
        if (name.length() > 255) {
            throw new CustomException(ErrorCode.THEME_NAME_TOO_LONG);
        }
    }

    private void validateDescriptionLength(String description) {
        if (description.length() > 255) {
            throw new CustomException(ErrorCode.THEME_DESCRIPTION_TOO_LONG);
        }
    }

    private void validateThumbnailUrlLength(String thumbnailUrl) {
        if (thumbnailUrl.length() > 255) {
            throw new CustomException(ErrorCode.THEME_THUMBNAIL_TOO_LONG);
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return Objects.equals(name, theme.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
