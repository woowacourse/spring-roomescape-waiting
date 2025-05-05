package roomescape.service.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import roomescape.auth.JwtTokenProvider;
import roomescape.domain.member.Member;
import roomescape.dto.member.LoginRequestDto;
import roomescape.dto.member.SignupRequestDto;
import roomescape.exception.member.InvalidMemberException;
import roomescape.repository.member.MemberRepository;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberService(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(LoginRequestDto loginRequestDto) {
        Member member = loginRequestDto.toEntity();

        if (!memberRepository.existsByEmailAndPassword(member)) {
            throw new InvalidMemberException("존재하지 않는 유저입니다");
        }

        return jwtTokenProvider.createToken(member);
    }

    public long signup(SignupRequestDto signupRequestDto) {
        Member member = signupRequestDto.toEntity();
        return memberRepository.add(member);
    }

    public Member getMemberById(long id) {
        return memberRepository.findById(id).orElseThrow(() -> new InvalidMemberException("존재하지 않는 유저입니다"));
    }
}
