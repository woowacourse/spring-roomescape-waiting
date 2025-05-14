package roomescape.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.dto.response.MemberResponseDto;

@Service
public class MemberService {
    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<MemberResponseDto> findAll() {
        return memberDao.findAll().stream()
                .map(MemberResponseDto::new)
                .collect(Collectors.toList());
    }
}
