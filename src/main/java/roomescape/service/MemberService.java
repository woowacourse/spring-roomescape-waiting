package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.service.dto.MemberResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.MemberJoinRequest;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse join(MemberJoinRequest memberRequest) {
        Member member = memberRequest.toUserMember();
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new RoomEscapeBusinessException("중복된 이메일입니다.");
        }

        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
         return memberRepository.findAll().stream()
                 .map(MemberResponse::from)
                 .toList();
    }

    @Transactional
    public void withdraw(Long id) {
        Member foundMember = memberRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));

        memberRepository.delete(foundMember);
    }

    @Transactional(readOnly = true)
    public MemberResponse findByEmailAndPassword(String email, String password) {
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));

        return MemberResponse.from(member);
    }
}
