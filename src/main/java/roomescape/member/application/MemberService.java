package roomescape.member.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthRole;
import roomescape.exception.auth.AuthorizationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.member.domain.MemberQueryRepository;
import roomescape.member.ui.dto.CreateMemberRequest;
import roomescape.member.ui.dto.MemberResponse.IdName;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberCommandRepository memberCommandRepository;
    private final MemberQueryRepository memberQueryRepository;

    public void create(final CreateMemberRequest request) {
        final Member member = new Member(
                request.name(),
                request.email(),
                request.password(),
                AuthRole.MEMBER
        );
        memberCommandRepository.save(member);
    }

    public void delete(final Long id) {
        final Member found = memberQueryRepository.getById(id);

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
