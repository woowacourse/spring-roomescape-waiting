package roomescape.member.application;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member create(Member member) {
        validateDuplicatedEmail(member);
        try {
            return memberRepository.save(member);
        } catch (DuplicateKeyException e) {
            throw new ViolationException("중복된 이메일입니다.");
        }
    }

    private void validateDuplicatedEmail(Member member) {
        boolean isDuplicatedEmail = memberRepository.findByEmail(member.getEmail())
                .isPresent();
        if (isDuplicatedEmail) {
            throw new ViolationException("중복된 이메일입니다.");
        }
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 Id의 사용자가 없습니다."));
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}
