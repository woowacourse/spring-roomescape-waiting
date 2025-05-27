package roomescape.member.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.controller.dto.MemberInfoResponse;
import roomescape.member.domain.Member;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    public static MemberInfo toDto(Member member) {
        return new MemberInfo(
                member.getId(),
                member.getName().getValue(),
                member.getEmail().getValue(),
                member.getRole());
    }

    public static MemberInfoResponse toResponse(MemberInfo memberInfo) {
        return new MemberInfoResponse(
                memberInfo.id(),
                memberInfo.name(),
                memberInfo.email()
        );
    }

    public static List<MemberInfoResponse> toResponses(List<MemberInfo> memberInfos) {
        return memberInfos.stream()
                .map(MemberConverter::toResponse)
                .toList();
    }
}
