package roomescape.service.dto.output;

import roomescape.domain.user.Member;

import java.util.List;

public record MemberOutput(long id, String name, String email, String password) {
    public static MemberOutput toOutput(final Member member) {
        return new MemberOutput(member.getId(), member.getName(), member.getEmail(), member.getPassword());
    }
    public static List<MemberOutput> toOutputs(final List<Member> members) {
        return members.stream()
                .map(MemberOutput::toOutput)
                .toList();
    }
}
