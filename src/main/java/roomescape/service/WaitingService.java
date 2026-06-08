package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.dto.request.WaitingRequestDto;

@Service
@Transactional
public class WaitingService {
    private final WaitingDao waitingDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public WaitingService(WaitingDao waitingDao, TimeDao timeDao, ThemeDao themeDao,
                          ReservationDao reservationDao) {
        this.waitingDao = waitingDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    public Waiting create(WaitingRequestDto waitingRequestDto, Member member) {
        Reservation reservation = reservationDao.findByThemeIdAndTimeIdAndDateAndStoreIdForUpdate(waitingRequestDto.themeId(),
                        waitingRequestDto.timeId(), waitingRequestDto.date(), waitingRequestDto.storeId())
                .orElseThrow(() -> new EntityNotFoundException("예약이 존재하지 않아 대기가 불가능합니다."));

        if (reservation.isOwnedBy(member.getId())) {
            throw new BusinessRuleViolationException("동일한 사용자의 예약이 존재합니다.");
        }

        if (waitingDao.existsByMemberIdAndDateAndTimeIdAndThemeIdAndStoreId(
                member.getId(),
                waitingRequestDto.date(),
                waitingRequestDto.timeId(),
                waitingRequestDto.themeId(),
                waitingRequestDto.storeId())) {
            throw new DuplicateEntityException("이미 대기 신청한 슬롯입니다.");
        }

        return waitingDao.insert(buildWaiting(waitingRequestDto, member));
    }

    public void delete(Long waitingId) {
        if (!waitingDao.delete(waitingId)) {
            throw new EntityNotFoundException("존재하지 않는 예약 대기입니다.");
        }
    }

    public void promoteFirstWaiting(Slot slot, LocalDateTime now) {
        waitingDao.findFirstForUpdate(slot.getDate(), slot.getTimeId(), slot.getThemeId(), slot.getStoreId())
                .ifPresent(first -> promote(first, now));
    }

    private void promote(Waiting waiting, LocalDateTime now) {
        if (waiting.isPast(now)) {
            return;
        }
        if (!waitingDao.delete(waiting.getId())) {
            return;
        }
        try {
            reservationDao.insert(waiting.toReservation(now));
        } catch (DuplicateKeyException e) {
            throw new DuplicateEntityException("이미 예약이 존재하여 대기를 전환할 수 없습니다.");
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

    private Waiting buildWaiting(WaitingRequestDto waitingRequestDto, Member member) {
        Time time = timeDao.findById(waitingRequestDto.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        Theme theme = themeDao.findById(waitingRequestDto.themeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
        return new Waiting(
                member,
                waitingRequestDto.date(),
                time,
                theme,
                waitingRequestDto.storeId()
        );
    }
}
