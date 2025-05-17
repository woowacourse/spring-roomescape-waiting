package roomescape.member.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthRole;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.member.domain.MemberQueryRepository;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.member.ui.dto.SignUpRequest;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberCommandRepository memberCommandRepository;
    private final MemberQueryRepository memberQueryRepository;

    public MemberResponse.IdName create(final SignUpRequest request) {
        final Member member = new Member(
                request.name(),
                request.email(),
                request.password(),
                AuthRole.MEMBER
        );

        final Member saved = memberCommandRepository.save(member);
        return new MemberResponse.IdName(saved.getId(), saved.getName());
    }

    public void delete(final Long id) {
        final Member found = memberQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        if (found.getRole() == AuthRole.ADMIN) {
            throw new AuthorizationException("관리자는 삭제할 수 없습니다.");
        }
        memberCommandRepository.deleteById(id);
    }

    public List<IdName> findAllNames() {
        return memberQueryRepository.findAll().stream()
                .map(IdName::from)
                .toList();
    }
}
