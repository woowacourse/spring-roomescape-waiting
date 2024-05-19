package roomescape.member.dto;

import roomescape.member.model.Member;
import roomescape.member.model.MemberEmail;
import roomescape.member.model.MemberName;
import roomescape.member.model.MemberPassword;
import roomescape.member.model.MemberRole;

public record MemberDto(
        Long id,
        MemberRole role,
        MemberEmail email,
        MemberName name,
        MemberPassword password
) {
    public static MemberDto from(Member member) {
        return new MemberDto(
                member.getId(),
                member.getRole(),
                member.getEmail(),
                member.getName(),
                member.getPassword()
        );
    }
}
