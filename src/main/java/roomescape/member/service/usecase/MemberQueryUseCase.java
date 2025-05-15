package roomescape.member.service.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.AuthenticationException;
import roomescape.common.exception.ErrorCode;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberConverter;

@Service
@RequiredArgsConstructor
public class MemberQueryUseCase {

    private final MemberRepository memberRepository;

    public Member get(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new AuthenticationException("등록된 회원이 아닙니다.", ErrorCode.MEMBER_NOT_FOUND));
    }

    public List<MemberInfo> getAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberConverter::toDto)
                .toList();
    }
}
