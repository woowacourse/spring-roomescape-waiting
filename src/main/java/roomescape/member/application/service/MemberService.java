package roomescape.member.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.util.JwtTokenProvider;
import roomescape.member.application.exception.MemberNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.presentation.dto.LoginRequest;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.SignupRequest;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberService(JwtTokenProvider jwtTokenProvider,
                         MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAllMembers() {

        List<Member> members = memberRepository.findAll();
        return members.stream().map(MemberResponse::from).toList();
    }

    public Member findMemberById(Long id) {

        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
    }

    public MemberResponse addMember(SignupRequest signupRequest) {

        Member member = new Member(null, signupRequest.name(), signupRequest.email(), signupRequest.password(),
                Role.USER);
        Member addedMember = memberRepository.save(member);
        return MemberResponse.from(addedMember);
    }

    public String createToken(LoginRequest loginRequest) {
        
        Member foundMember = memberRepository.findMemberByEmailAndPassword(loginRequest.email(),
                loginRequest.password()).orElseThrow(MemberNotFoundException::new);
        String token = jwtTokenProvider.createToken(foundMember);
        return token;
    }
}
