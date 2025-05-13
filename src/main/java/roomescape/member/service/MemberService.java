package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.dto.MemberInfo;

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
