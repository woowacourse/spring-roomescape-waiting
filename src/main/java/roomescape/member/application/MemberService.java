package roomescape.member.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthRole;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.member.ui.dto.SignUpRequest;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse.IdName create(final SignUpRequest request) {
        final Member member = new Member(
                request.name(),
                request.email(),
                request.password(),
                AuthRole.MEMBER
        );

        final Member saved = memberRepository.save(member);
        return new MemberResponse.IdName(saved.getId(), saved.getName());
    }

    public void delete(final Long memberId) {
        final Member found = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        if (found.getRole() == AuthRole.ADMIN) {
            throw new AuthorizationException("관리자 계정은 삭제할 수 없습니다.");
        }
        memberRepository.deleteById(memberId);
    }

    public List<IdName> findAllNames() {
        return memberRepository.findAll().stream()
                .map(IdName::from)
                .toList();
    }
}
