package roomescape.member.service.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.AuthenticationException;
import roomescape.common.exception.ErrorCode;
import roomescape.common.exception.NotFoundException;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberId;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberConverter;

@Service
@RequiredArgsConstructor
public class MemberQueryUseCase {

    private final MemberRepository memberRepository;

    public Account getAccount(LoginRequest loginRequest) {
        return memberRepository.findAccountByEmail(MemberEmail.from(loginRequest.email())
        ).orElseThrow(() -> new NotFoundException("등록된 이메일이 존재하지 않습니다."));
    }

    public Member get(MemberId id) {
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
