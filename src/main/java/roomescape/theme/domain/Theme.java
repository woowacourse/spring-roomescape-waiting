package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.exception.BusinessException;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        validateIsNonNull(name);
        validateIsNonNull(description);
        validateIsNonNull(thumbnail);

        validateIsEmpty(name);
        validateIsEmpty(description);
        validateIsEmpty(thumbnail);

        this.id = null;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("테마 정보는 null 일 수 없습니다.");
        }
    }

    private void validateIsEmpty(final String something) {
        if (something.isEmpty()) {
            throw new BusinessException("테마 정보는 비어있을 수 없습니다.");
        }
    }
}
