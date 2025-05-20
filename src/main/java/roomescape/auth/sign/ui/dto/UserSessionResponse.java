package roomescape.auth.sign.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.auth.session.UserSession;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.user.domain.User;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record UserSessionResponse(Long userId,
                                  String userName,
                                  String userRole) {

    public UserSessionResponse {
        validate(userId, userName, userRole);
    }

    public static UserSessionResponse from(final UserSession userSession) {
        return new UserSessionResponse(
                userSession.id().getValue(),
                userSession.name().getValue(),
                userSession.role().name()
        );
    }

    public static UserSessionResponse from(final User user) {
        return new UserSessionResponse(
                user.getId().getValue(),
                user.getName().getValue(),
                user.getRole().name()
        );
    }

    private void validate(final Long userId,
                          final String userName,
                          final String userRole) {
        Validator.of(UserSessionResponse.class)
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.userName, userName, DomainTerm.USER_NAME.label())
                .validateNotNull(Fields.userRole, userRole, DomainTerm.USER_ROLE.label());
    }
}
