package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.common.exception.UnprocessableException;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.SlotRepository;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final SlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(SlotRepository slotRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Reservation reserve(ReservationCreateRequest request, LocalDateTime now) {
        ReservationTime time = reservationTimeRepository.getById(request.getTimeId());
        Theme theme = themeRepository.getById(request.getThemeId());
        ReservationDate date = new ReservationDate(request.getDate());

        Slot slot = slotRepository.findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> slotRepository.save(Slot.create(date, time, theme, now)));
        if (slot.isPast(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }

        Reservations existing = reservationRepository.findBySlotId(slot.getId());
        if (existing.hasByName(request.getName())) {
            throw new ConflictException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }

        return reservationRepository.save(existing.join(request.getName(), slot));
    }

    public Reservation find(long id) {
        Reservation reservation = reservationRepository.getById(id);

        if (reservation.isWaiting()) {
            Reservations slotReservations = reservationRepository.findBySlotId(reservation.getSlotId());
            return reservation.withRank(slotReservations.rankOf(reservation));
        }

        return reservation;
    }

    public Reservations findList(String name) {
        if (name != null) {
            return reservationRepository.findByName(name);
        }
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation update(ReservationUpdateRequest request, long id, LocalDateTime now) {
        Reservation existing = find(id);

        ReservationTime newTime = reservationTimeRepository.getById(request.getTimeId());
        Theme newTheme = themeRepository.getById(request.getThemeId());
        ReservationDate newDate = new ReservationDate(request.getDate());

        Slot newSlot = slotRepository.findByDateAndTimeAndTheme(newDate, newTime, newTheme)
                .orElseGet(() -> slotRepository.save(Slot.create(newDate, newTime, newTheme, now)));
        if (newSlot.isPast(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }

        Reservations slotReservations = reservationRepository.findBySlotId(newSlot.getId()).excluding(id);
        boolean isSelf = existing.getSlotId().equals(newSlot.getId()) && existing.isSameName(request.getName());
        if (slotReservations.hasByName(request.getName()) && !isSelf) {
            throw new ConflictException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }

        Reservation updated = slotReservations.join(request.getName(), newSlot);
        reservationRepository.update(id, updated);

        boolean slotChanged = !existing.getSlotId().equals(newSlot.getId());
        if (slotChanged && existing.isApproved()) {
            reservationRepository.findBySlotId(existing.getSlotId())
                    .firstWaiting()
                    .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
        }

        return find(id);
    }

    @Transactional
    public void cancel(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = reservationRepository.getById(reservationId);
        Slot slot = slotRepository.getById(reservation.getSlotId());

        if (slot.isPast(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }

        if (!reservation.isSameName(name)) {
            throw new UnauthorizedException("예약자명이 다릅니다.");
        }

        reservationRepository.deleteById(reservationId);

        if (reservation.isApproved()) {
            reservationRepository.findBySlotId(slot.getId())
                    .firstWaiting()
                    .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
        }
    }
}
