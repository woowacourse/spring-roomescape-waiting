package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.common.exception.UnprocessableException;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.repository.JdbcSlotRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {
    private final JdbcSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(JdbcSlotRepository slotRepository,
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
        ReservationTime time = findTimeById(request.getTimeId());
        Theme theme = findThemeById(request.getThemeId());
        ReservationDate date = new ReservationDate(request.getDate());

        Slot slot = slotRepository.findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> slotRepository.save(Slot.create(date, time, theme, now)));
        if (slot.isPast(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }

        if (reservationRepository.existsBySlotIdAndName(slot.getId(), request.getName())) {
            throw new ConflictException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }

        boolean hasApproved = reservationRepository.existsApprovedBySlotId(slot.getId());
        Status status = hasApproved ? Status.WAITING : Status.APPROVED;

        Reservation reservation = Reservation.create(request.getName(), status, slot);
        return reservationRepository.save(reservation);
    }

    public Reservation find(long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. 입력을 확인해 주세요."));

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

        ReservationTime newTime = findTimeById(request.getTimeId());
        Theme newTheme = findThemeById(request.getThemeId());
        ReservationDate newDate = new ReservationDate(request.getDate());

        Slot newSlot = slotRepository.findByDateAndTimeAndTheme(newDate, newTime, newTheme)
                .orElseGet(() -> slotRepository.save(Slot.create(newDate, newTime, newTheme, now)));
        if (newSlot.isPast(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }

        boolean isSelf = existing.getSlotId().equals(newSlot.getId()) && existing.isSameName(request.getName());
        if (reservationRepository.existsBySlotIdAndName(newSlot.getId(), request.getName()) && !isSelf) {
            throw new ConflictException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }

        boolean hasApproved = reservationRepository.existsApprovedBySlotId(newSlot.getId());
        Status newStatus = hasApproved ? Status.WAITING : Status.APPROVED;

        Reservation updated = Reservation.create(request.getName(), newStatus, newSlot);
        reservationRepository.update(id, updated);

        boolean slotChanged = !existing.getSlotId().equals(newSlot.getId());
        if (slotChanged && existing.isApproved()) {
            reservationRepository.findFirstWaitingBySlotId(existing.getSlotId())
                    .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
        }

        return find(id);
    }

    @Transactional
    public void cancel(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = find(reservationId);

        Slot slot = slotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 슬롯입니다."));

        if (slot.isPast(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }

        if (!reservation.isSameName(name)) {
            throw new UnauthorizedException("예약자명이 다릅니다.");
        }

        reservationRepository.deleteById(reservationId);

        if (reservation.isApproved()) {
            reservationRepository.findFirstWaitingBySlotId(slot.getId())
                    .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
        }
    }

    private ReservationTime findTimeById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다. 입력을 확인해 주세요."));
    }

    private Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다. 입력을 확인해 주세요."));
    }
}
