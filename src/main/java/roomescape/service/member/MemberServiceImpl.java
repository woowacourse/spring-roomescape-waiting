package roomescape.service.member;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.enums.Role;
import roomescape.dto.login.LoginRequest;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.signup.SignupRequest;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.repository.member.MemberRepository;
import roomescape.util.JwtTokenProvider;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberServiceImpl( JwtTokenProvider jwtTokenProvider,
                             MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    public List<MemberResponse> findAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream().map(member -> MemberResponse.from(member)).toList();
    }

    @Override
    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    public MemberResponse addMember(SignupRequest signupRequest) {
        Member member = new Member(null, signupRequest.name(), signupRequest.email(), signupRequest.password(),
                Role.USER);
        Member addedMember = memberRepository.save(member);
        return MemberResponse.from(addedMember);
    }

    @Override
    public String createToken(LoginRequest loginRequest) {
        Member foundMember = memberRepository.findMemberByEmailAndPassword(loginRequest.email(),
                loginRequest.password()).orElseThrow(()->new MemberNotFoundException());
        String token = jwtTokenProvider.createToken(foundMember);
        return token;
    }
}
