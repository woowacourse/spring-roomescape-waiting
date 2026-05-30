package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.dto.request.WaitingRequestDto;

@Service
@Transactional
public class WaitingService {
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;

    public WaitingService(WaitingDao waitingDao, ReservationDao reservationDao) {
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
    }

    public Waiting create(WaitingRequestDto waitingRequestDto, Member member) {
        Reservation reservation = reservationDao.findByThemeIdAndTimeIdAndDateAndStoreIdForUpdate(waitingRequestDto.themeId(),
                        waitingRequestDto.timeId(), waitingRequestDto.date(), waitingRequestDto.storeId())
                .orElseThrow(() -> new BusinessRuleViolationException("예약이 존재하지 않아 대기가 불가능합니다."));

        Waiting waiting = Waiting.create(member, reservation);

        if (waitingDao.existsByMemberIdAndDateAndTimeIdAndThemeIdAndStoreId(
                member.getId(),
                waitingRequestDto.date(),
                waitingRequestDto.timeId(),
                waitingRequestDto.themeId(),
                waitingRequestDto.storeId())) {
            throw new DuplicateEntityException("이미 대기 신청한 슬롯입니다.");
        }

        return waitingDao.insert(waiting);
    }

    public void delete(Long waitingId) {
        if (!waitingDao.delete(waitingId)) {
            throw new EntityNotFoundException("존재하지 않는 예약 대기입니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAll() {
        return waitingDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllByMemberId(Long memberId) {
        return waitingDao.findAllByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllByStoreId(Long storeId) {
        return waitingDao.findAllByStoreId(storeId);
    }


}
