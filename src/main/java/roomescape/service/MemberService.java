package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.exception.NotFoundMemberException;
import roomescape.exception.UnableCreateMemberException;
import roomescape.exception.UnauthorizedException;
import roomescape.persistence.MemberRepository;
import roomescape.service.param.LoginMemberParam;
import roomescape.service.param.RegisterMemberParam;
import roomescape.service.result.MemberResult;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResult login(final LoginMemberParam loginMemberParam) {
        Member member = memberRepository.findByEmail(loginMemberParam.email())
                .orElseThrow(() -> new NotFoundMemberException(loginMemberParam.email() + "에 해당하는 유저가 없습니다."));

        if (member.isPasswordNotMatched(loginMemberParam.password())) {
            throw new UnauthorizedException("비밀 번호가 일치하지 않습니다.");
        }

        return MemberResult.from(member);
    }

    public MemberResult create(final RegisterMemberParam registerMemberParam) {
        Member member = new Member(
                registerMemberParam.name(),
                MemberRole.USER,
                registerMemberParam.email(),
                registerMemberParam.password()
        );
        memberRepository.findByEmail(registerMemberParam.email())
                .ifPresent(m -> {
                    throw new UnableCreateMemberException("이미 존재하는 이메일입니다.");
                });

        Member savedMember = memberRepository.save(member);
        return MemberResult.from(savedMember);
    }

    public MemberResult findById(final Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundMemberException(id + "에 해당하는 유저가 없습니다."));
        return MemberResult.from(member);
    }

    public List<MemberResult> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResult::from)
                .toList();
    }
}
