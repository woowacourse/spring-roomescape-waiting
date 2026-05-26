package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.MemberDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.dto.request.WaitingRequestDto;

@Service
public class WaitingService {
    private final WaitingDao waitingDao;
    private final MemberDao memberDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;

    public WaitingService(WaitingDao waitingDao, MemberDao memberDao, TimeDao timeDao, ThemeDao themeDao) {
        this.waitingDao = waitingDao;
        this.memberDao = memberDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
    }

    public Waiting create(WaitingRequestDto waitingRequestDto) {
        Member member = memberDao.findById(waitingRequestDto.memberId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버입니다."));
        Time time = timeDao.findById(waitingRequestDto.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        Theme theme = themeDao.findById(waitingRequestDto.themeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
        return waitingDao.insert(new Waiting(
                member,
                waitingRequestDto.date(),
                time,
                theme,
                waitingRequestDto.storeId()
        ));
    }

    public void delete(Long waitingId) {
        if (!waitingDao.delete(waitingId)) {
            throw new EntityNotFoundException("존재하지 않는 예약 대기입니다.");
        }
    }

    public List<Waiting> findAll() {
        return waitingDao.findAll();
    }

    public List<Waiting> findAllByMemberId(Long memberId) {
        return waitingDao.findAllByMemberId(memberId);
    }

    public List<Waiting> findAllByStoreId(Long storeId) {
        return waitingDao.findAllByStoreId(storeId);
    }
}
