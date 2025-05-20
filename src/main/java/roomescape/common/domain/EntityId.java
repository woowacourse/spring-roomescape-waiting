package roomescape.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.validate.Validator;

@Getter
@FieldNameConstants
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@EqualsAndHashCode
public abstract class EntityId {

    private Long value;

    protected EntityId(final Long value) {
        validate(value);

        this.value = value;
    }

    private void validate(final Long value) {
        Validator.of(EntityId.class)
                .validateNotNull(Fields.value, value, DomainTerm.DOMAIN_ID.label());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + value + ")";
    }
}
