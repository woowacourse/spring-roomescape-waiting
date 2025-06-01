package roomescape.auth.session;

import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

@FieldNameConstants
public record Session(Long userId,
                      UserName name,
                      UserRole role) {

    public Session {
        validate(userId, name, role);
    }

    private void validate(final Long id, final UserName name, final UserRole role) {
        Validator.of(Session.class)
                .validateNotNull(Fields.userId, id, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.name, name, DomainTerm.USER_NAME.label())
                .validateNotNull(Fields.role, role, DomainTerm.USER_ROLE.label());
    }
}
