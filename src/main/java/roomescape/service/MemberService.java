package roomescape.service;

import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.dto.MemberResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    public List<MemberResponse> findAll() {
        return memberRepository.findAll().getMembers().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public Member findById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
    }
}
