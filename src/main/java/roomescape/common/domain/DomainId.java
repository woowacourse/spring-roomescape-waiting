package roomescape.common.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@EqualsAndHashCode
public class DomainId {

    private Long id;

    public Long getId() {
        if (id != null) {
            return id;
        }
        throw new IllegalStateException("저장되지 않아 식별할 수 없습니다.");
    }
}
