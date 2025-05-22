package roomescape.integration.api;

import roomescape.domain.member.Member;

public record RestLoginMember(Member member, String sessionId) {
}
