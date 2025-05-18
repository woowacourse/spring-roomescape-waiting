package roomescape.service.member;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.dto.member.LoginRequestDto;
import roomescape.dto.member.SignupRequestDto;
import roomescape.exception.member.InvalidMemberException;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;
import roomescape.infrastructure.auth.member.UserInfo;
import roomescape.repository.member.MemberRepository;

@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository,
                         JwtTokenProvider jwtTokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(LoginRequestDto loginRequestDto) {
        Member requestMember = loginRequestDto.toEntity();

        Member member = memberRepository.findByUsername(requestMember.getUsername())
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 유저입니다"));

        if (!passwordEncoder.matches(loginRequestDto.password(), member.getPassword())) {
            throw new InvalidMemberException("유효하지 않은 인증입니다");
        }

        return jwtTokenProvider.createToken(member);
    }

    @Transactional
    public long signup(SignupRequestDto signupRequestDto) {
        boolean isDuplicateUserExist = memberRepository.existsByUsername(signupRequestDto.email());
        if (isDuplicateUserExist) {
            throw new InvalidMemberException("이미 존재하는 유저입니다.");
        }

        Member member = signupRequestDto.toEntity();
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        Member newMember = new Member(null, member.getUsername(), encodedPassword, member.getName(), member.getRole());
        return memberRepository.save(newMember).getId();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new InvalidMemberException("존재하지 않는 유저입니다"));
    }

    public Member getMemberByToken(String token) {
        UserInfo userInfo = jwtTokenProvider.resolveToken(token);
        return getMemberById(userInfo.id());
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}
