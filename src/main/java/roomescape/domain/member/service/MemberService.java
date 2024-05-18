package roomescape.domain.member.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.repository.MemberRepository;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 member를 조회 했습니다."));
    }

    public Member findMemberByEmailAndPassword(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new EntityNotFoundException("이메일 또는 비밀번호를 잘못 입력했습니다."));
    }
}
