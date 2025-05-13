package roomescape.common.domain;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants
@EqualsAndHashCode
public class DomainId {

    private final Long value;
    private final boolean assigned;

    private DomainId(final Long value, final boolean assigned) {
        validate(value);

        this.value = value;
        this.assigned = assigned;
    }

    public static DomainId unassigned() {
        return new DomainId(UUID.randomUUID().getMostSignificantBits(), false);
    }

    public static DomainId assigned(final Long id) {
        return new DomainId(id, true);
    }

    public Long getValue() {
        if (assigned) {
            return value;
        }
        throw new IllegalStateException("저장되지 않아 식별할 수 없습니다.");
    }

    private static void validate(final Long value) {
        Validator.of(DomainId.class)
                .notNullField(Fields.value, value);
    }
}
