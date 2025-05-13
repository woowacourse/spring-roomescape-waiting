package roomescape.common.domain;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import roomescape.common.validate.Validator;

import java.util.Objects;

@FieldNameConstants
public abstract class DomainId {

    private final Long value;
    @Getter
    private final boolean assigned;

    protected DomainId(final Long value, final boolean assigned) {
        validate(value, assigned);

        this.value = value;
        this.assigned = assigned;
    }

    private void validate(final Long value,
                          final boolean assigned) {
        if (assigned) {
            Validator.of(DomainId.class)
                    .validateNotNull(Fields.value, value, DomainTerm.DOMAIN_ID.label());
        }
    }

    public Long getValue() {
        requireAssigned();
        return value;
    }

    public void requireAssigned() {
        if (assigned) {
            return;
        }
        throw new IllegalStateException("식별자가 할당되지 않았습니다.");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DomainId other = (DomainId) o;

        if (this.value == null || other.value == null) {
            return false;
        }

        return this.assigned == other.assigned && this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        if (value == null) {
            return System.identityHashCode(this);
        }
        return Objects.hash(value, assigned);
    }

    @Override
    public String toString() {
        if (assigned) {
            return getClass().getSimpleName() + "(" + value + ")";
        }
        return getClass().getSimpleName() + "(unassigned)";
    }
}
