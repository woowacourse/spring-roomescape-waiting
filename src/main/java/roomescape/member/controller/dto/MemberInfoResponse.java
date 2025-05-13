package roomescape.member.controller.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;
import roomescape.member.auth.vo.MemberInfo;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record MemberInfoResponse(
        Long id,
        String name,
        String email
) {

    public MemberInfoResponse {
        validate(id, name, email);
    }

    private void validate(final Long id,
                          final String name,
                          final String email) {
        Validator.of(MemberInfo.class)
                .notNullField(Fields.id, id)
                .notNullField(Fields.name, name)
                .notNullField(Fields.email, email);
    }
}
