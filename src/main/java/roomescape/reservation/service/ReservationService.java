package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waitings;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Long save(ReservationCreateRequest reservationCreateRequest, LoginMemberInToken loginMemberInToken) {
        if (reservationRepository.existsByDateAndReservationTimeIdAndThemeId(reservationCreateRequest.date(),
                reservationCreateRequest.timeId(), reservationCreateRequest.themeId())) {
            Reservation reservation = getValidatedReservation(reservationCreateRequest, loginMemberInToken,
                    Status.WAITING);
            return reservationRepository.save(reservation).getId();
        }

        Reservation reservation = getValidatedReservation(reservationCreateRequest, loginMemberInToken, Status.SUCCESS);
        return reservationRepository.save(reservation).getId();
    }

    private Reservation getValidatedReservation(ReservationCreateRequest reservationCreateRequest,
                                                LoginMemberInToken loginMemberInToken,
                                                Status status) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationCreateRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(reservationCreateRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = getValidatedMemberByRole(reservationCreateRequest, loginMemberInToken);

        return reservationCreateRequest.toReservation(member, theme, reservationTime, status);
    }

    private Member getValidatedMemberByRole(ReservationCreateRequest reservationCreateRequest,
                                            LoginMemberInToken loginMemberInToken) {
        return memberRepository.findById(loginMemberInToken.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        return ReservationResponse.toResponse(reservation);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    public List<ReservationResponse> findAllBySearch(ReservationSearchRequest reservationSearchRequest) {
        Theme theme = themeRepository.findById(reservationSearchRequest.themeId()).get();
        Member member = memberRepository.findById(reservationSearchRequest.memberId()).get();
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme,
                        reservationSearchRequest.dateFrom(), reservationSearchRequest.dateTo()).stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    public List<MyReservationResponse> findAllByMemberId(Long memberId) {
        List<Reservation> waitingReservation = reservationRepository.findAllByStatus(Status.WAITING);
        Waitings waitings = new Waitings(waitingReservation);

        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(reservation -> MyReservationResponse.toResponse(reservation,
                        waitings.findMemberRank(reservation, memberId)))
                .toList();
    }

    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id).get();
        List<Reservation> waitingReservation = reservationRepository.findAllByDateAndReservationTimeIdAndThemeIdAndStatus(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId(), Status.WAITING);
        if (!waitingReservation.isEmpty()) {
            Waitings waitings = new Waitings(waitingReservation);
            Reservation firstWaiting = waitings.getFirstWaiting();
            firstWaiting.changeSuccess();
        }
        reservationRepository.deleteById(id);
    }

    public List<WaitingResponse> findWaiting() {
        return reservationRepository.findAllByStatus(Status.WAITING).stream()
                .map(reservation -> WaitingResponse.toResponse(reservation.getMember(), reservation.getTheme(),
                        reservation, reservation.getTime()))
                .toList();
    }
}
