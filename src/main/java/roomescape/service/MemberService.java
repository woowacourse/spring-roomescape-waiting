package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnAuthorizedException;
import roomescape.service.param.LoginMemberParam;
import roomescape.service.param.RegisterMemberParam;
import roomescape.service.result.MemberResult;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResult login(final LoginMemberParam loginMemberParam) {
        Member member = memberRepository.findByEmail(loginMemberParam.email())
                .orElseThrow(() -> new UnAuthorizedException("이메일이나 비밀번호가 일치하지 않습니다."));

        if (!Objects.equals(member.getPassword(), loginMemberParam.password())) {
            throw new UnAuthorizedException("이메일이나 비밀번호가 일치하지 않습니다.");
        }

        return MemberResult.from(member);
    }

    @Transactional
    public MemberResult create(final RegisterMemberParam registerMemberParam) {
        Member member = memberRepository.save(Member.createNew(registerMemberParam.name(), MemberRole.USER, registerMemberParam.email(), registerMemberParam.password()));

        return MemberResult.from(member);
    }

    public MemberResult getById(final Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("memberId", id));
        return MemberResult.from(member);
    }

    public List<MemberResult> getAll() {
        return memberRepository.findAll().stream()
                .map(MemberResult::from)
                .toList();
    }
}
