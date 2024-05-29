package roomescape.application;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.dto.MemberResponse;
import roomescape.dto.MemberSignUpRequest;
import roomescape.exception.RoomescapeException;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAll() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse save(MemberSignUpRequest memberSignUpRequest) {
        if (memberRepository.existsByEmail(new Email(memberSignUpRequest.email()))) {
            throw new RoomescapeException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        }
        Member member = memberRepository.save(memberSignUpRequest.toEntity());
        return MemberResponse.from(member);
    }
}
