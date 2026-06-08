package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.domain.service.WaitingPromotionResult;
import roomescape.domain.service.WaitingPromotionService;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.*;
import roomescape.domain.theme.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.command.ReservationCommand;
import roomescape.service.command.ReservationUpdateCommand;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationUserCommandService {

    private final WaitingPromotionService promotionService;
    private final ReservationDao reservationDao;
    private final ReservationWaitingDao waitingDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    private ReservationTime findTimeReference(Long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeReference(Long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 테마입니다."));
    }

    @Transactional
    public Reservation create(ReservationCommand command) {
        Slot slot = Slot.from(
                Schedule.from(
                        command.date(),
                        findTimeReference(command.timeId())),
                findThemeReference(command.themeId())
        );

        if (reservationDao.findBySlot(slot).isPresent()) {
            throw new DuplicateException("해당 날짜와 시간에 이미 예약이 존재합니다.");
        }

        Long savedId = reservationDao.create(Reservation.create(command.name(), slot, LocalDateTime.now(clock)));
        return reservationDao.findById(savedId)
                .orElseThrow(() -> new ResourceNotFoundException("예약이 정상적으로 생성되지 않았습니다."));
    }

    @Transactional
    public Reservation update(Long reservationId, UserName name, ReservationUpdateCommand command) {
        Reservation current = reservationDao.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));

        current.validateOwnedBy(name);

        Slot slot = Slot.from(
                Schedule.from(
                        command.date(),
                        findTimeReference(command.timeId())),
                current.getReservationTheme()
        );

        if (reservationDao.existsBySlotAndIdNot(reservationId, slot)) {
            throw new DuplicateException("변경하려는 시간에 이미 다른 예약이 존재합니다.");
        }

        reservationDao.updateDateAndTime(current.withSlot(slot, LocalDateTime.now(clock)));
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("업데이트한 예약을 찾을 수 없습니다."));
    }

    @Transactional
    public void cancel(Long reservationId, UserName name) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));

        LocalDateTime now = LocalDateTime.now(clock);

        reservation.validateOwnedBy(name);
        reservation.validateCancelable(now);

        List<ReservationWaiting> waitings = waitingDao.findAllBySlot(reservation.getSlot());
        Optional<WaitingPromotionResult> promotion = promotionService.promote(waitings);

        reservationDao.delete(reservation);

        if (promotion.isPresent()) {
            reservationDao.create(promotion.get().promotedReservation());
            waitingDao.delete(promotion.get().targetWaiting());
        }
    }
}
