package roomescape.member.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthRole;
import roomescape.exception.auth.AuthorizationException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.member.ui.dto.CreateMemberRequest;
import roomescape.member.ui.dto.MemberResponse.IdName;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void create(final CreateMemberRequest request) {
        final Member member = new Member(
                request.name(),
                request.email(),
                request.password(),
                AuthRole.MEMBER
        );
        memberRepository.save(member);
    }

    @Transactional
    public void delete(final Long id) {
        final Member found = memberRepository.getByIdOrThrow(id);

        if (found.getRole() == AuthRole.ADMIN) {
            throw new AuthorizationException("관리자는 삭제할 수 없습니다.");
        }
        memberRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<IdName> findAllNames() {
        return memberRepository.findAll().stream()
                .map(IdName::from)
                .toList();
    }
}
