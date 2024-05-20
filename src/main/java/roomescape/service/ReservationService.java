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
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationDetailRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
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
    public Reservation saveMemberReservation(ReservationRequest request) {
        ReservationTime reservationTime = getReservationTimeById(request.timeId());
        rejectPastReservation(request.date(), reservationTime);
        // 프록시 또는 완전한 엔티티
        ReservationDetail reservationDetail = getReservationDetail(request, reservationTime);
        Member member = getMember(request);
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
        if (reservationRepository.existsByDetailAndMember(detail, member)) {
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

    @Transactional
    public Reservation saveReservation(ReservationRequest request) {
        return new Reservation(null, null, null);
    }

    @Transactional
    public void deleteReservation(Long id) {
        ReservationDetail reservationDetail = getReservationById(id);
        reservationDetailRepository.delete(reservationDetail);
    }

    private ReservationDetail getReservationDetail(ReservationRequest request, ReservationTime reservationTime) {
        return reservationDetailRepository.findByDateAndThemeIdAndTimeId(
                request.date(), request.timeId(), request.themeId()
        ).orElseGet(() -> reservationDetailRepository.save(request.toReservationDetail(
                reservationTime, getThemeById(request.themeId())
        )));
    }

    private Member getMember(ReservationRequest request) {
        return memberRepository.findById(request.memberId())
                .orElseThrow(AuthenticationFailureException::new);
    }

    private ReservationDetail getReservationById(Long id) {
        return reservationDetailRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    private ReservationTime getReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }

    private Theme getThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
