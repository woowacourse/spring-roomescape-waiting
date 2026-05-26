package roomescape.theme.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Theme {

    private Long id;
    private String name;
    private String thumbnailImageUrl;
    private String description;
    private LocalTime durationTime;
    private LocalDateTime deletedAt;

    public Theme withId(final Long id) {
        return Theme.builder()
                .id(id)
                .name(this.name)
                .thumbnailImageUrl(this.thumbnailImageUrl)
                .description(this.description)
                .durationTime(this.durationTime)
                .build();
    }

    public Theme delete(final Clock clock) {
        return Theme.builder()
                .id(id)
                .name(name)
                .thumbnailImageUrl(thumbnailImageUrl)
                .description(description)
                .durationTime(durationTime)
                .deletedAt(LocalDateTime.now(clock))
                .build();
    }
}
