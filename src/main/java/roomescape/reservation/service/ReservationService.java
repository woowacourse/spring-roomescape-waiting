package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.global.auth.LoginMember;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.waiting.domain.WaitingWithRank;
import roomescape.reservation.waiting.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository,
            final WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse createReservation(final CreateReservationRequest request) {
        final Reservation reservation = convertToReservation(request);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> getFilteredReservations(
            final Long memberId,
            final Long themeId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    ) {
        final List<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                memberId,
                themeId,
                dateFrom,
                dateTo);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<MyReservationResponse> getMyReservations(final LoginMember loginMember) {
        final List<Reservation> reservations = reservationRepository.findAllByMemberIdOrderByDateDesc(loginMember.id());
        final List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(loginMember.id());

        return Stream.concat(
                reservations.stream()
                        .map(reservation -> new MyReservationResponse(reservation, ReservationStatus.RESERVED.getDescription())),
                waitings.stream()
                        .map(waitingWithRank -> new MyReservationResponse(
                                waitingWithRank.getWaiting(),
                                waitingWithRank.getDescription(ReservationStatus.WAITING))
                        )
        ).toList();
    }

    public void cancelReservationById(final long id) {
        reservationRepository.deleteById(id);
    }

    private Reservation convertToReservation(final CreateReservationRequest request) {
        LocalDate date = request.date();

        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BadRequestException("예약자를 찾을 수 없습니다."));
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new BadRequestException("테마가 존재하지 않습니다."));
        final ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new BadRequestException("예약 시간이 존재하지 않습니다."));
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId())) {
            throw new BadRequestException("해당 시간에 이미 예약이 존재합니다.");
        }
        return Reservation.register(member, date, time, theme);
    }
}
