package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.common.exception.BusinessException;
import roomescape.common.util.time.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.member.exception.MemberNotFound;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.MyReservationResponse;
import roomescape.reservation.domain.*;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.WaitingResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.exception.ReservationTimeException;
import roomescape.reservationTime.presentation.dto.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.presentation.dto.ThemeResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final DateTime dateTime;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
        final DateTime dateTime,
        final ReservationRepository reservationRepository,
        final ReservationTimeRepository reservationTimeRepository,
        final ThemeRepository themeRepository,
        final MemberRepository memberRepository,
        final WaitingRepository waitingRepository
    ) {
        this.dateTime = dateTime;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new ReservationTimeException("예약 시간을 찾을 수 없습니다."));
        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new ThemeException("테마를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException("멤버를 찾을 수 없습니다."));

        List<Reservation> reservations = reservationRepository.findBy(request.themeId(), request.date());
        validateExistDuplicateReservation(reservations, time);

        Reservation reservation = Reservation.createWithoutId(request.date(), time, theme, member, Status.RESERVED);
        validateCanReserveDateTime(reservation, dateTime.now());
        reservation = reservationRepository.save(reservation);

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public WaitingResponse createWaiting(final ReservationRequest request, final Long memberId) {
        Reservation reservation = reservationRepository.findBy(request.date(), request.timeId(), request.themeId())
            .orElseThrow(() -> new ReservationException("예약 정보가 없습니다."));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFound("멤버를 찾을 수 없습니다."));

        validateNotReservationOwner(reservation, member);
        validateCanReserveDateTime(reservation, dateTime.now());
        validateDuplicateWaiting(reservation, member);

        Waiting waiting = waitingRepository.save(Waiting.createWithoutId(reservation, member));
        return WaitingResponse.from(waiting);
    }

    private void validateNotReservationOwner(final Reservation reservation, final Member member) {
        if (reservation.isSameMember(member)) {
            throw new ReservationException("예약자는 예약대기를 할 수 없습니다.");
        }
    }

    private void validateDuplicateWaiting(Reservation reservation, Member member) {
        if (waitingRepository.exists(reservation.getId(), member.getId())) {
            throw new ReservationException("이미 예약대기 중입니다.");
        }
    }

    private void validateExistDuplicateReservation(final List<Reservation> reservations, final ReservationTime time) {
        boolean isBooked = reservations.stream()
            .anyMatch(reservation -> reservation.isSameTime(time));

        if (isBooked) {
            throw new ReservationException("이미 예약이 존재합니다.");
        }
    }

    private void validateCanReserveDateTime(final Reservation reservation, final LocalDateTime now) {
        if (reservation.isCannotReserveDateTime(now)) {
            throw new ReservationException("예약할 수 없는 날짜와 시간입니다.");
        }
    }

    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAll().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    @Transactional
    public void deleteReservationById(final Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ReservationException("예약을 찾을 수 없습니다."));

        List<Waiting> waitings = waitingRepository.findByReservationId(id);
        if (waitings.isEmpty()) {
            reservationRepository.deleteById(id);
            return;
        }

        Waiting firstWaiting = waitings.getFirst();
        reservation.reAssignedTo(firstWaiting.getMember());

        waitingRepository.deleteById(firstWaiting.getId());
    }

    @Transactional
    public void deleteWaiting(final Long waitingId) {
        waitingRepository.findById(waitingId)
            .orElseThrow(() -> new ReservationException("예약 대기를 찾을 수 없습니다."));

        waitingRepository.deleteById(waitingId);
    }

    public List<ReservationResponse> searchReservationWithCondition(final SearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findBy(
            condition.memberId(), condition.themeId(),
            condition.dateFrom(), condition.dateTo()
        );

        return reservations.stream()
            .map(reservation -> new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime().getId(), reservation.getTime().getStartAt()),
                new ThemeResponse(reservation.getTheme().getId(), reservation.getTheme().getName(),
                    reservation.getTheme().getDescription(), reservation.getTheme().getThumbnail()),
                new MemberResponse(reservation.getMember().getId(), reservation.getMember().getName())
            ))
            .toList();
    }

    public List<MyReservationResponse> getMemberReservations(final LoginMemberInfo loginMemberInfo) {
        Member member = memberRepository.findById(loginMemberInfo.id())
            .orElseThrow(() -> new BusinessException("멤버를 찾을 수 없습니다."));

        List<Reservation> reservations = reservationRepository.findBy(loginMemberInfo.id());

        List<WaitingWithRank> waitingWithRanks = waitingRepository.findByMemberId(loginMemberInfo.id());

        return Stream.concat(
            reservations.stream().map(MyReservationResponse::from),
            waitingWithRanks.stream().map(MyReservationResponse::from)
        ).toList();
    }

    public List<ReservationResponse> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();

        return waitings.stream()
            .map(ReservationResponse::from)
            .toList();
    }
}
