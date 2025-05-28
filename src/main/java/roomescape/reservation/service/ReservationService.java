package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.util.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.infrastructure.dto.ReservationWithRank;
import roomescape.reservation.presentation.dto.request.ReservationConditionRequest;
import roomescape.reservation.presentation.dto.request.ReservationRequest;
import roomescape.reservation.presentation.dto.request.ReservationWaitingRequest;
import roomescape.reservation.presentation.dto.response.MyReservationResponse;
import roomescape.reservation.presentation.dto.response.ReservationResponse;
import roomescape.reservation.presentation.dto.response.WaitingReservationResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
public class ReservationService {

    private final DateTime dateTime;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            final DateTime dateTime, final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository, final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        this.dateTime = dateTime;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        if (reservationRepository.hasReservedReservation(request.date(), request.timeId(), request.themeId())) {
            throw new IllegalArgumentException("이미 예약이 존재합니다.");
        }
        Reservation reservation = makeReservation(request.timeId(), request.themeId(),
                memberId, request.date(), ReservationStatus.RESERVED);
        Reservation save = reservationRepository.save(reservation);
        return ReservationResponse.from(save);
    }

    public ReservationResponse createWaitingReservation(final ReservationWaitingRequest waitingRequest,
                                                        final Long memberId) {
        validateCanMakeWaitingReservation(waitingRequest, memberId);
        Reservation reservation = makeReservation(waitingRequest.time(), waitingRequest.theme(),
                memberId, waitingRequest.date(), ReservationStatus.WAITED);
        Reservation save = reservationRepository.save(reservation);
        return ReservationResponse.from(save);
    }

    public List<ReservationResponse> getAllReservations(ReservationConditionRequest request) {
        if (request.isEmpty()) {
            return reservationRepository.findAll().stream()
                    .map(ReservationResponse::from)
                    .toList();
        }
        return reservationRepository.findByMemberIdAndThemeIdAndDate(request.memberId(), request.themeId(),
                        request.dateFrom(), request.dateTo())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<WaitingReservationResponse> getWaitingReservations() {
        List<Reservation> waitingReservations = reservationRepository.findAllWaitingReservations(dateTime.now());
        return waitingReservations.stream()
                .map(WaitingReservationResponse::from)
                .toList();
    }

    public void deleteReservationById(final Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isEmpty()) {
            throw new IllegalArgumentException("기존 예약이 존재하지 않습니다.");
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void changeWaitStatusToReserved(final Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약대기입니다."));

        if (reservationRepository.hasReservedReservation(reservation.getDate(), reservation.timeId(),
                reservation.themeId())) {
            throw new IllegalArgumentException("기존 확정 예약이 취소 되지 않았습니다.");
        }
        reservation.changeToReserve();
    }

    public List<MyReservationResponse> getMyReservations(final Long id) {
        List<ReservationWithRank> reservations = reservationRepository.findReservationWithRankByMemberId(id);
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    private Reservation makeReservation(Long timeId, Long themeId, Long memberId, LocalDate date,
                                        ReservationStatus status) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시간입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return Reservation.createWithoutId(dateTime.now(), member, date, time,
                theme, status);
    }

    private void validateCanMakeWaitingReservation(ReservationWaitingRequest waitingRequest, Long memberId) {
        if (!reservationRepository.hasReservedReservation(waitingRequest.date(), waitingRequest.time(),
                waitingRequest.theme())) {
            throw new IllegalArgumentException("예약 가능한 상태에서는 대기할 수 없습니다.");
        }
        if (reservationRepository.hasSameReservation(waitingRequest.date(), waitingRequest.time(), memberId,
                waitingRequest.theme(), ReservationStatus.RESERVED)) {
            throw new IllegalArgumentException("이미 예약을 완료하였습니다.");
        }
        if (reservationRepository.hasSameReservation(waitingRequest.date(), waitingRequest.time(), memberId,
                waitingRequest.theme(), ReservationStatus.WAITED)) {
            throw new IllegalArgumentException("이미 예약 대기를 신청했습니다.");
        }
    }
}
