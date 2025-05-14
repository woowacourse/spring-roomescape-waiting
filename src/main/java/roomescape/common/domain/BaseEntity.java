package roomescape.common.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Objects;

@FieldNameConstants
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        final BaseEntity that = (BaseEntity) o;

        if (o == null || getClass() != o.getClass()) return false;

        if (this.id == null || that.id == null) {
            return false;
        }

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return System.identityHashCode(this);
        }
        return Objects.hash(id);
    }
}


