package roomescape.service;

import static roomescape.exception.RoomescapeExceptionCode.MEMBER_NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Member;
import roomescape.dto.LoginRequest;
import roomescape.dto.MemberResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.util.JwtProvider;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public MemberService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    public String login(LoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();
        Member member = getMemberByEmailAndPassword(email, password);
        return jwtProvider.createToken(member.getId());
    }

    private Member getMemberByEmailAndPassword(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new RoomescapeException(MEMBER_NOT_FOUND));
    }

    public Member getMemberById(long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new RoomescapeException(MEMBER_NOT_FOUND));
    }

    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
