package roomescape.common.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected BaseEntity(final Long id) {
        requireAssigned();
        this.id = id;
    }

    private void requireAssigned() {
        if (isAssigned()) {
            return;
        }
        throw new IllegalStateException("식별자가 할당되지 않았습니다.");
    }

    private boolean isAssigned() {
        return id != null;
    }
}


