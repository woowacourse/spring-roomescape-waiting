package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberPassword;
import roomescape.exception.member.EmailDuplicatedException;
import roomescape.exception.member.UnauthorizedEmailException;
import roomescape.exception.member.UnauthorizedPasswordException;
import roomescape.global.JwtManager;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.member.MemberLoginRequest;
import roomescape.service.dto.member.MemberResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtManager jwtManager;

    public MemberService(MemberRepository memberRepository, JwtManager jwtManager) {
        this.memberRepository = memberRepository;
        this.jwtManager = jwtManager;
    }

    public void signup(MemberCreateRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicatedException();
        }
        memberRepository.save(request.toMember());
    }

    public String login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(UnauthorizedEmailException::new);
        MemberPassword requestPassword = new MemberPassword(request.getPassword());
        if (member.isMismatchedPassword(requestPassword)) {
            throw new UnauthorizedPasswordException();
        }
        return jwtManager.generateToken(member);
    }

    public List<MemberResponse> findAllMemberNames() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::new)
                .toList();
    }
}
