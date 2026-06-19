package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.*;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.UserReservationRequest;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ReservationDao;
import roomescape.service.dto.ReservationWithWaitingOrder;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final int EMPTY_RESERVATION_COUNT = 0;

    private final ReservationDao reservationDao;
    private final MemberService memberService;
    private final ScheduleService scheduleService;

    public ReservationService(ReservationDao reservationDao, MemberService memberService, ScheduleService scheduleService) {
        this.reservationDao = reservationDao;
        this.memberService = memberService;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public Long saveReservationByAdmin(AdminReservationRequest request) {
        Member member = memberService.getMemberById(request.memberId());
        LocalDateTime now = LocalDateTime.now();
        Schedule schedule = scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId());

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
    public Long saveConfirmedReservationByPayment(long scheduleId, long memberId) {
        LocalDateTime now = LocalDateTime.now();
        scheduleService.lockById(scheduleId);
        Schedule schedule = scheduleService.getById(scheduleId);
        Member member = memberService.getMemberById(memberId);
        validatePaymentOrderCreatable(member, schedule, now);

        Reservation reservation = Reservation.createBy(
                member,
                schedule,
                ReservationStatus.RESERVED,
                now
        );

        return reservationDao.save(reservation);
    }

    @Transactional
    public Long saveWaitingReservation(UserReservationRequest request, Member member) {
        LocalDateTime now = LocalDateTime.now();
        Schedule schedule = scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId());
        validateWaitingCreatable(member, schedule, now);

        Reservation reservation = Reservation.createBy(
                member,
                schedule,
                ReservationStatus.WAITING,
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

        Reservation canceled = reservation.cancelByAdmin(now);

        reservationDao.changeStatusWithUpdateAt(canceled);
        promoteWaitingReservation(reservation, scheduleId);
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

    public List<ReservationWithWaitingOrder> findAll() {
        return reservationDao.findAll();
    }

    public List<ReservationWithWaitingOrder> findByMember(Member member) {
        return reservationDao.findByMemberId(member.getId());
    }

    public void validateBeforeConfirm(long memberId, long scheduleId, LocalDateTime now) {
        Schedule schedule = scheduleService.getById(scheduleId);
        Member member = memberService.getMemberById(memberId);

        validatePaymentOrderCreatable(member, schedule, now);
    }

    public void validatePaymentOrderCreatable(Member member, Schedule schedule, LocalDateTime now) {
        validateNotPastSchedule(schedule, now);

        if (reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 해당 스케줄에 본인의 예약이 존재합니다.");
        }

        if (reservationDao.countReservationByScheduleId(schedule.getId()) != EMPTY_RESERVATION_COUNT) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE, "이미 예약된 슬롯은 결제할 수 없습니다. 예약 대기를 이용해주세요.");
        }
    }

    private void validateWaitingCreatable(Member member, Schedule schedule, LocalDateTime now) {
        validateNotPastSchedule(schedule, now);

        if (reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 해당 스케줄에 본인의 예약이 존재합니다.");
        }

        if (reservationDao.countReservationByScheduleId(schedule.getId()) == EMPTY_RESERVATION_COUNT) {
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "예약 가능한 슬롯은 결제 후 예약해주세요.");
        }
    }

    private void validateNotPastSchedule(Schedule schedule, LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(schedule.getDate(), schedule.getTime().getStartAt());

        if (!reservationDateTime.isAfter(now)) {
            throw new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "과거 시각으로는 결제를 시작할 수 없습니다.");
        }
    }

    private void promoteWaitingReservation(Reservation reserved, long scheduleId) {
        if (reserved.isReserved()) {
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
}
