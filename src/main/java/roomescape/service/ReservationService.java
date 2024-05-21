package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDetail;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationDetailRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.reservation.CancelReservationException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.service.dto.request.member.MemberInfo;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.ReservationSearchCond;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.UserReservationResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationDetailRepository reservationDetailRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Reservation saveMemberReservation(ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.getById(request.timeId());
        rejectPastReservation(request.date(), reservationTime);
        // 프록시 또는 완전한 엔티티
        ReservationDetail reservationDetail = reservationDetailRepository.getByDateAndThemeIdAndTimeId(
                request.date(), request.themeId(), request.timeId());
        Member member = memberRepository.getById(request.memberId());
        rejectDuplicateReservation(reservationDetail, member);

        // 바로 예약 만들기, 기존 예약이 존재하면 waiting으로, 아니라면 resolved로
        Reservation reservation = createReservation(reservationDetail, member);
        return reservationRepository.save(reservation);
    }

    private void rejectPastReservation(LocalDate date, ReservationTime time) {
        LocalDateTime localDateTime = date.atTime(time.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidDateTimeReservationException();
        }
    }

    private void rejectDuplicateReservation(ReservationDetail detail, Member member) {
        if (reservationRepository.existsByDetailAndMemberAndStatusNot(detail, member, Status.CANCELED)) {
            throw new DuplicatedReservationException();
        }
    }

    private Reservation createReservation(ReservationDetail reservationDetail, Member member) {
        boolean reservationExists = reservationRepository.existsByDetailAndStatus(reservationDetail, Status.RESERVED);
        if (reservationExists) {
            return new Reservation(member, reservationDetail, Status.WAITING);
        }
        return new Reservation(member, reservationDetail, Status.RESERVED);
    }

    public List<ReservationResponse> findAllReservation() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllReservationByConditions(ReservationSearchCond cond) {
        List<Reservation> reservations = reservationRepository.findByPeriodAndThemeAndMember(
                cond.start(), cond.end(), cond.themeId(), cond.memberId());

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<UserReservationResponse> findAllByMemberId(Long memberId) {
        return null;
    }

    @Transactional
    public Reservation saveReservation(ReservationRequest request) {
        return new Reservation(null, null, null);
    }

    @Transactional
    public void cancelReservation(Long reservationId, MemberInfo memberInfo) {
        Reservation reservation = reservationRepository.getById(reservationId);
        rejectUnauthorizedCancel(memberInfo, reservation);
        reservation.cancel();
    }

    private void rejectUnauthorizedCancel(MemberInfo memberInfo, Reservation reservation) {
        if (reservation.isNotOwner(memberInfo.id())) {
            throw new CancelReservationException("다른 회원의 예약을 취소할 수 없습니다.");
        }
        if (reservation.isReserved()) {
            throw new CancelReservationException("예약은 어드민만 취소할 수 있습니다.");
        }
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.getById(reservationId);
        rejectAlreadyCanceled(reservation);
        reservation.cancel();
    }

    private void rejectAlreadyCanceled(Reservation reservation) {
        if (reservation.isCanceled()) {
            throw new CancelReservationException("이미 취소된 예약입니다.");
        }
    }
}
