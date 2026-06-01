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
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Reservation reserve(ReservationCreateRequest request, LocalDateTime now) {
        ReservationTime reservationTime = findReservationTimeByTimeId(request.getTimeId());
        Theme theme = findThemeByThemeId(request.getThemeId());

        validateIsDuplicateReservation(request.getTimeId(), request.getThemeId(), request.getDate(), request.getName());

        boolean hasApproved = reservationRepository.existsApprovedByTimeAndThemeAndDate(
                request.getTimeId(), request.getThemeId(), request.getDate());
        Status status = hasApproved ? Status.WAITING : Status.APPROVED;

        Reservation reservation = Reservation.create(
                new ReservationName(request.getName()),
                new ReservationDate(request.getDate()), reservationTime,
                theme,
                now, status);
        return reservationRepository.save(reservation);
    }

    public Reservation find(long id) {
        return findReservationById(id);
    }

    public List<Reservation> findList(String name) {
        return findByNameOrAll(name);
    }

    private List<Reservation> findByNameOrAll(String name) {
        if (name != null) {
            return reservationRepository.findAllByName(name);
        }
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation update(ReservationUpdateRequest request, long id, LocalDateTime now) {
        Reservation reservation = findReservationById(id);

        ReservationDate newDate = new ReservationDate(request.getDate());
        ReservationTime newTime = findReservationTimeByTimeId(request.getTimeId());
        Theme newTheme = findThemeByThemeId(request.getThemeId());

        validateIsDuplicateReservation(request.getTimeId(), request.getThemeId(), request.getDate(), request.getName());

        boolean hasApprovedInNewSlot = reservationRepository.existsApprovedByTimeAndThemeAndDate(
                newTime.getId(), newTheme.getId(), newDate.getDate());
        Status newStatus = hasApprovedInNewSlot ? Status.WAITING : Status.APPROVED;

        Reservation target = Reservation.create(new ReservationName(request.getName()), newDate, newTime,
                newTheme, now, newStatus);

        Reservation updated = reservationRepository.update(id, target);

        promoteFirstWaiting(reservation);

        return updated;
    }

    @Transactional
    public void cancel(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservationById(reservationId);

        if (reservation.isPast(now)) {
            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
        }

        if (!reservation.isSameName(name)) {
            throw new UnauthorizedException("예약자명이 다릅니다.");
        }

        reservationRepository.deleteById(reservationId);

        promoteFirstWaiting(reservation);
    }

    private ReservationTime findReservationTimeByTimeId(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다. 입력을 확인해 주세요."));
    }

    private Theme findThemeByThemeId(long id) {
        return themeRepository.findById(id).orElseThrow(
                () -> new NotFoundException("존재하지 않는 테마입니다. 입력을 확인해 주세요."));
    }

    private void validateIsDuplicateReservation(long timeId, long themeId, LocalDate date, String name) {
        if (reservationRepository.existsByTimeAndThemeAndDateAndName(timeId, themeId, date, name)) {
            throw new ConflictException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }
    }

    private Reservation findReservationById(long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException("존재하지 않는 예약입니다. 입력을 확인해 주세요."));
    }

    private void promoteFirstWaiting(Reservation reservation) {
        if (reservation.isApproved()) {
            reservationRepository.findFirstWaitingByTimeAndThemeAndDate(
                            reservation.getTime(), reservation.getTheme(), reservation.getDate())
                    .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
        }
    }
}
