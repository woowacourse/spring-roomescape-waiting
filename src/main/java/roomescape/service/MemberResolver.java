package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.repository.MemberRepository;

@Component
public class MemberResolver {

    private final MemberRepository memberRepository;

    public MemberResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member resolve(String name) {
        return memberRepository.findByName(name)
            .orElseGet(() -> saveOrFind(name));
    }

    private Member saveOrFind(String name) {
        try {
            return memberRepository.saveAndFlush(new Member(name));
        } catch (DataIntegrityViolationException e) {
            return memberRepository.findByName(name)
                .orElseThrow(() -> e);
        }
    }
}
