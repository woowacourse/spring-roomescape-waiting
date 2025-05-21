package roomescape.business.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.persistence.repository.MemberRepository;
import roomescape.presentation.dto.MemberResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}
