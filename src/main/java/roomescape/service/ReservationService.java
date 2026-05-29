package roomescape.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.EntityNotFoundException;
import roomescape.repository.ReservationSlotRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationCondition;
import roomescape.service.command.ReservationChangeCommand;
import roomescape.service.command.ReservationCommand;
import roomescape.service.result.ReservationSlotResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationSlotRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public ReservationSlotResult reserve(ReservationCommand command) {
        ReservationSlot slot = reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition())
                .orElseGet(() -> {
                    Theme theme = findThemeWithThrow(command.themeId());
                    ReservationTime time = findTimeWithThrow(command.timeId());
                    return ReservationSlot.createSlot(command.date(), theme, time);
                });
        slot.reserve(command.name());
        return ReservationSlotResult.from(reservationRepository.save(slot));
    }

    @Transactional
    public ReservationSlotResult change(long reservationId, ReservationChangeCommand command) {
        ReservationSlot current = reservationRepository.findByReservationIdForUpdate(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
        Reservation reservation = current.findReservedReservation(reservationId);

        ReservationTime newTime = findTimeWithThrow(command.timeId());
        if (current.isSameSlot(command.date(), newTime)) {
            return ReservationSlotResult.from(current, reservation);
        }

        return moveReservation(reservation, current, command.date(), newTime);
    }

    private ReservationSlotResult moveReservation(Reservation reservation, ReservationSlot current,
                                        LocalDate date, ReservationTime newTime) {
        ReservationSlot target = findOrCreateSlot(date, current.getTheme(), newTime);
        Reservation moved = target.reserve(reservation.getName());

        current.cancelReservation(reservation.getId());
        reservationRepository.save(current);

        ReservationSlot saved = reservationRepository.save(target);
        return ReservationSlotResult.from(saved, saved.findReservationByNameAndStatus(reservation.getName(), moved.getStatus()));
    }

    private ReservationSlot findOrCreateSlot(LocalDate date, Theme theme, ReservationTime time) {
        ReservationCondition condition = new ReservationCondition(date, theme.getId(), time.getId());
        return reservationRepository.findByDateAndThemeAndTimeForUpdate(condition)
                .orElseGet(() -> ReservationSlot.createSlot(date, theme, time));
    }

    @Transactional
    public ReservationSlotResult addWaiting(ReservationCommand command) {
        ReservationSlot slot = reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition())
                .orElseGet(() -> {
                    Theme theme = findThemeWithThrow(command.themeId());
                    ReservationTime time = findTimeWithThrow(command.timeId());
                    return ReservationSlot.createSlot(command.date(), theme, time);
                });
        Reservation added = slot.joinWaitingList(command.name());
        ReservationSlot saved = reservationRepository.save(slot);
        return ReservationSlotResult.from(saved, saved.findReservationByNameAndStatus(command.name(), added.getStatus()));
    }

    @Transactional
    public void cancelReservation(long reservationId) {
        ReservationSlot slot = reservationRepository.findByReservationIdForUpdate(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));

        slot.cancelReservation(reservationId);
        reservationRepository.save(slot);
    }

    private Theme findThemeWithThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .filter(Theme::isActive)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마 정보입니다."));
    }

    private ReservationTime findTimeWithThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .filter(ReservationTime::isActive)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간 정보입니다."));
    }

    public ReservationSlotResult getReservation(long reservationId) {
        ReservationSlot slot = findReservationByReservationIdWithThrow(reservationId);
        Reservation reservationReservation = slot.findReservedReservation(reservationId);
        return ReservationSlotResult.from(slot, reservationReservation);
    }

    private ReservationSlot findReservationByReservationIdWithThrow(long reservationId) {
        return reservationRepository.findByReservationIdForUpdate(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 정보입니다."));
    }
}
