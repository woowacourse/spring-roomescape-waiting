package roomescape.auth.session;

import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

@FieldNameConstants
public record Session(UserId id,
                      UserName name,
                      UserRole role) {

    public Session {
        validate(id, name, role);
    }

    private void validate(final UserId id, final UserName name, final UserRole role) {
        Validator.of(Session.class)
                .validateNotNull(Fields.id, id, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.name, name, DomainTerm.USER_NAME.label())
                .validateNotNull(Fields.role, role, DomainTerm.USER_ROLE.label());
    }
}
