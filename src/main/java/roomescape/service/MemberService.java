package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.MemberRepository;
import roomescape.domain.user.Email;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotExistException;
import roomescape.service.dto.input.MemberCreateInput;
import roomescape.service.dto.input.MemberLoginInput;
import roomescape.service.dto.output.MemberCreateOutput;
import roomescape.service.dto.output.MemberLoginOutput;
import roomescape.service.dto.output.MemberOutput;
import roomescape.service.dto.output.TokenLoginOutput;
import roomescape.util.TokenProvider;

import java.util.List;

import static roomescape.exception.ExceptionDomainType.MEMBER;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public MemberService(final MemberRepository memberRepository, final TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public MemberCreateOutput createMember(final MemberCreateInput memberCreateInput) {
        if (memberRepository.existsByEmail(new Email(memberCreateInput.email()))) {
            throw new AlreadyExistsException(String.format("%s 는 이미 존재합니다.", memberCreateInput.email()));
        }
        final Member member = memberRepository.save(memberCreateInput.toMember());
        return MemberCreateOutput.toOutput(member);
    }

    public List<MemberOutput> getAllMembers() {
        final List<Member> members = memberRepository.findAll();
        return MemberOutput.toOutputs(members);
    }

    public MemberLoginOutput loginMember(final MemberLoginInput memberLoginInput) {
        final Member member = memberRepository.findByEmail(new Email(memberLoginInput.email()))
                .orElseThrow(() -> new NotExistException(String.format("%s 해당하는 멤버가 없습니다", memberLoginInput.email())));
        if (member.isNotEqualPassword(memberLoginInput.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return new MemberLoginOutput(tokenProvider.generateToken(member));
    }

    public TokenLoginOutput loginToken(final String token) {
        final long id = tokenProvider.decodeToken(token)
                .getId();
        final Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotExistException(MEMBER, id));
        return TokenLoginOutput.toOutput(member);
    }
}
