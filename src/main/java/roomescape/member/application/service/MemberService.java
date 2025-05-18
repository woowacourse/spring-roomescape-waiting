package roomescape.member.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.application.dto.MemberInfo;
import roomescape.member.domain.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberInfo> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberInfo::new)
                .toList();
    }
}
