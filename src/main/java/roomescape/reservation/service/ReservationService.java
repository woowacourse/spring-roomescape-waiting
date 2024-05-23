package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
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

    public Reservation save(ReservationCreateRequest reservationCreateRequest, LoginMemberInToken loginMemberInToken) {
        Reservation reservation = getValidatedReservation(reservationCreateRequest, loginMemberInToken);
        validateDuplicateReservation(reservation);

        return reservationRepository.save(reservation);
    }

    private Reservation getValidatedReservation(ReservationCreateRequest reservationCreateRequest,
            LoginMemberInToken loginMemberInToken) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationCreateRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(reservationCreateRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(loginMemberInToken.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return reservationCreateRequest.toReservation(member, theme, reservationTime);
    }

    private void validateDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsByDateAndReservationTimeStartAt(reservation.getDate(), reservation.getTime()
                .getStartAt())) {
            throw new IllegalArgumentException("중복된 예약이 있습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllBySearch(ReservationSearchRequest reservationSearchRequest) {
        Member member = memberRepository.findById(reservationSearchRequest.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Theme theme = themeRepository.findById(reservationSearchRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        return reservationRepository.findAllByMemberAndThemeAndDateBetween(
                        member,
                        theme,
                        reservationSearchRequest.dateFrom(),
                        reservationSearchRequest.dateTo()
                ).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<MyReservationResponse> findAllByMemberId(Long memberId) {
        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(reservation -> new MyReservationResponse(reservation, ReservationStatus.RESERVATION))
                .toList();
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}
