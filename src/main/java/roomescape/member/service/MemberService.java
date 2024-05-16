package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.DuplicateSaveException;
import roomescape.global.exception.NoSuchRecordException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Name;
import roomescape.member.dto.JoinRequest;
import roomescape.member.dto.MemberResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchRecordException("id: " + id + " 해당하는 회원을 찾을 수 없습니다"));
    }

    public MemberResponse joinMember(JoinRequest joinRequest) {
        Email joinEmail = new Email(joinRequest.email());
        if (memberRepository.existsByEmail(joinEmail)) {
            throw new DuplicateSaveException("중복되는 이메일의 회원이 존재합니다");
        }

        Name joinName = new Name(joinRequest.name());
        if (memberRepository.existsByName(joinName)) {
            throw new DuplicateSaveException("중복되는 이름의 회원이 존재합니다");
        }

        Member member = joinRequest.toMember();
        return new MemberResponse(memberRepository.save(member));
    }
}
