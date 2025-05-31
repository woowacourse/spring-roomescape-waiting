package roomescape.member.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.application.dto.MemberInfo;
import roomescape.member.domain.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public List<MemberInfo> findMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberInfo::new)
                .toList();
    }
}
