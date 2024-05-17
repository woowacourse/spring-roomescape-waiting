package roomescape.service;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.dto.member.MemberResponse;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 존재하지 않는 사용자 입니다.",
                        new Throwable("member_id : " + id)
                ));

        Member member2 = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 존재하지 않는 사용자 입니다.",
                        new Throwable("member_id : " + id)
                ));

        boolean contains = entityManager.contains(member2);
        System.out.println(contains);
        return MemberResponse.from(member);
    }
}
