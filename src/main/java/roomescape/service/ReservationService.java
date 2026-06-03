package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.*;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.UserReservationRequest;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.MemberDao;
import roomescape.repository.ReservationDao;
import roomescape.service.dto.ReservationInfoResult;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final int EMPTY_RESERVATION_COUNT = 0;

    private final ReservationDao reservationDao;
    private final MemberDao memberDao;
    private final ScheduleService scheduleService;

    public ReservationService(ReservationDao reservationDao, MemberDao memberDao, ScheduleService scheduleService) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public Long saveReservation(AdminReservationRequest request) {
        Member member = getMemberById(request.memberId());
        return saveReservation(request.date(), request.timeId(), request.themeId(), member);
    }

    @Transactional
    public Long saveReservation(UserReservationRequest request, Member member) {
        return saveReservation(request.date(), request.timeId(), request.themeId(), member);
    }

    private Long saveReservation(
            java.time.LocalDate date,
            Long timeId,
            Long themeId,
            Member member
    ) {
        LocalDateTime now = LocalDateTime.now();
        Schedule schedule = scheduleService.getOrCreateScheduleForUpdate(date, timeId, themeId);

        if (reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 해당 스케줄에 본인의 예약이 존재합니다.");
        }

        Reservation reservation = Reservation.createBy(
                member,
                schedule,
                calculateReservationStatus(schedule.getId()),
                now
        );

        return reservationDao.save(reservation);
    }

    @Transactional
    public void cancelReservationByAdmin(long reservationId) {
        LocalDateTime now = LocalDateTime.now();
        long scheduleId = getScheduleIdByReservationId(reservationId);
        scheduleService.lockById(scheduleId);
        Reservation reservation = getById(reservationId);

        if (reservation.isAlreadyCanceled()) {
            return;
        }

        Reservation changed = reservation.cancelByAdmin(now);

        reservationDao.changeStatusWithUpdateAt(changed);
        promoteWaitingReservation(reservation, changed.getSchedule().getId());
    }

    @Transactional
    public void cancelReservation(long reservationId, Member member) {
        LocalDateTime now = LocalDateTime.now();
        long scheduleId = getScheduleIdByReservationId(reservationId);
        scheduleService.lockById(scheduleId);
        Reservation reservation = getById(reservationId);

        if (reservation.isAlreadyCanceled()) {
            return;
        }

        Reservation changed = reservation.cancelBy(
                member,
                now
        );

        reservationDao.changeStatusWithUpdateAt(changed);
        promoteWaitingReservation(reservation, changed.getSchedule().getId());
    }

    public List<ReservationResponse> findAll() {
        LocalDateTime now = LocalDateTime.now();
        return reservationDao.findAll()
                .stream()
                .map(result -> toReservationResponse(result, now))
                .toList();
    }

    public List<ReservationResponse> findByMember(Member member) {
        LocalDateTime now = LocalDateTime.now();
        return reservationDao.findByMemberId(member.getId())
                .stream()
                .map(result -> toReservationResponse(result, now))
                .toList();
    }

    private ReservationResponse toReservationResponse(ReservationInfoResult result, LocalDateTime now) {
        return ReservationResponse.from(
                result.reservation(),
                result.order(),
                now
        );
    }

    private void promoteWaitingReservation(Reservation changed, long scheduleId) {
        if (changed.isReserved()) {
            Optional<Reservation> reservations = reservationDao.findFirstByScheduleIdAndStatus(scheduleId, ReservationStatus.WAITING);
            reservations.ifPresent(reservation
                    -> reservationDao.promoteToReserved(reservation.getId())
            );
        }
    }

    private ReservationStatus calculateReservationStatus(long scheduleId) {
        if (countReservationBySchedule(scheduleId) == EMPTY_RESERVATION_COUNT) {
            return ReservationStatus.RESERVED;
        }

        return ReservationStatus.WAITING;
    }

    private int countReservationBySchedule(long scheduleId) {
        return reservationDao.countReservationByScheduleId(scheduleId);
    }

    private Reservation getById(long id) {
        return reservationDao.findById(id).orElseThrow(()
                -> new RoomescapeException(DomainErrorCode.NOT_FOUND_RESERVATION, "해당 ID의 예약이 존재하지 않습니다. ID: " + id)
        );
    }

    private long getScheduleIdByReservationId(long id) {
        return reservationDao.findScheduleIdById(id).orElseThrow(()
                -> new RoomescapeException(DomainErrorCode.NOT_FOUND_RESERVATION, "해당 ID의 예약이 존재하지 않습니다. ID: " + id)
        );
    }

    private Member getMemberById(Long id) {
        return memberDao.findById(id).orElseThrow(()
                -> new RoomescapeException(DomainErrorCode.INVALID_INPUT, "해당 ID의 회원이 존재하지 않습니다. ID: " + id)
        );
    }
}
