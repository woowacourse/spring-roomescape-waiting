package roomescape.reservation.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.common.util.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
public class ReservationService {

    private final DateTime dateTime;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(DateTime dateTime, ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, MemberRepository memberRepository, WaitingRepository waitingRepository) {
        this.dateTime = dateTime;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시간입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Optional<Member> findMember = memberRepository.findById(memberId);
        if (findMember.isEmpty()) {
            throw new IllegalArgumentException("존재 하지 않는 유저입니다.");
        }

        Reservation reservation = Reservation.createWithoutId(dateTime.now(), findMember.get(), request.date(), time,
                theme);

        if (reservationRepository.existsByDateAndTimeStartAtAndThemeId(reservation.getDate(),
                reservation.getReservationTime(), reservation.getThemeId())) {
            throw new IllegalArgumentException("이미 예약이 존재합니다.");
        }

        Reservation save = reservationRepository.save(reservation);

        return ReservationResponse.from(save);
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

    public void deleteReservationById(final Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        reservationRepository.deleteById(id);

        List<Waiting> waitings = waitingRepository.findByDateAndThemeIdAndTimeIdOrderByCreatedAtAsc(
                reservation.getDate(),
                reservation.getThemeId(),
                reservation.getTimeId()
        );

        if (!waitings.isEmpty()) {
            approveWaiting(waitings);
        }

    }

    private void approveWaiting(List<Waiting> waitings) {
        Waiting firstWaiting = waitings.get(0);

        Reservation newReservation = Reservation.createWithoutId(
                dateTime.now(),
                firstWaiting.getMember(),
                firstWaiting.getDate(),
                firstWaiting.getTime(),
                firstWaiting.getTheme()
        );
        reservationRepository.save(newReservation);

        waitingRepository.delete(firstWaiting);
    }

    public List<MyReservationResponse> getMyReservations(final Long id) {
        List<Reservation> confirmedReservations = reservationRepository.findByMemberId(id);
        List<MyReservationResponse> confirmedResponses = confirmedReservations.stream()
                .map(MyReservationResponse::from)
                .toList();

        List<Waiting> waitingReservations = waitingRepository.findByMemberId(id);
        List<MyReservationResponse> waitingResponses = waitingReservations.stream()
                .map(waiting -> {
                    long rank = calculateWaitingRank(waiting);
                    return MyReservationResponse.fromWaiting(waiting, rank);
                })
                .toList();

        return Stream.concat(confirmedResponses.stream(), waitingResponses.stream())
                .sorted(Comparator.comparing(MyReservationResponse::date))
                .toList();
    }

    private long calculateWaitingRank(Waiting waiting) {
        return waitingRepository.countByDateAndThemeIdAndTimeIdAndCreatedAtBefore(
                waiting.getDate(),
                waiting.getTheme().getId(),
                waiting.getTime().getId(),
                waiting.getCreatedAt()
        ) + 1;
    }
}
