package roomescape.auth.dto;

import roomescape.member.domain.Member;

public record LoggedInMember(Long id, String name, String email, boolean isAdmin) {
    public static LoggedInMember from(Member member) {
        return new LoggedInMember(member.getId(), member.getName(), member.getEmail(), member.isAdmin());
    }
}
