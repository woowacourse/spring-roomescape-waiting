package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.util.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.ReservationWaitingRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingReservationResponse;
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
        Reservation reservation = makeReservation(request.timeId(), request.themeId(),
                memberId, request.date(), ReservationStatus.RESERVED);

        if (reservationRepository.hasReservedReservation(reservation)) {
            throw new IllegalArgumentException("이미 예약이 존재합니다.");
        }

        Reservation save = reservationRepository.save(reservation);
        return ReservationResponse.from(save);
    }

    public ReservationResponse createWaitingReservation(final ReservationWaitingRequest waitingRequest,
                                                        final Long memberId) {
        Reservation reservation = makeReservation(waitingRequest.time(), waitingRequest.theme(),
                memberId, waitingRequest.date(), ReservationStatus.WAITED);

        if (!reservationRepository.hasReservedReservation(reservation)) {
            throw new IllegalArgumentException("예약 가능한 상태에서는 대기할 수 없습니다.");
        }

        if (reservationRepository.hasSameReservation(reservation)) {
            throw new IllegalArgumentException("이미 예약 대기를 신청했습니다");
        }

        Reservation save = reservationRepository.save(reservation);
        return ReservationResponse.from(save);
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

    public List<ReservationResponse> getReservations(ReservationConditionRequest request) {
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

    @Transactional
    public void deleteReservationById(final Long id) {
        reservationRepository.deleteById(id);
    }

    public List<MyReservationResponse> getMyReservations(final Long id) {
        List<ReservationWithRank> reservations = reservationRepository.findReservationWithRankByMemberId(id);
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
