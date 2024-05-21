package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.MemberJoinRequest;
import roomescape.service.dto.response.MemberResponse;
import roomescape.service.dto.response.MemberResponses;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResponse join(MemberJoinRequest memberRequest) {
        Member member = memberRequest.toUserMember();
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new RoomEscapeBusinessException("중복된 이메일입니다.");
        }

        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    public MemberResponses findAll() {
        List<MemberResponse> members = memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();

        return new MemberResponses(members);
    }

    public void withdraw(Long id) {
        Member foundMember = memberRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));

        memberRepository.delete(foundMember);
    }

    public MemberResponse findByEmailAndPassword(String email, String password) {
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));

        return MemberResponse.from(member);
    }
}
