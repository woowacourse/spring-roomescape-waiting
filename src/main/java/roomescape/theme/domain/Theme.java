package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theme {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    @Builder
    private Theme(final Long id, @NonNull final String name, @NonNull final String description,
                  @NonNull final String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme of(
            final String name,
            final String description,
            final String thumbnail
    ) {
        return builder()
                .id(null)
                .name(name)
                .description(description)
                .thumbnail(thumbnail)
                .build();
    }
}
