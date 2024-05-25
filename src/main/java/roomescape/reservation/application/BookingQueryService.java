package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingQueryService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    public BookingQueryService(ReservationRepository reservationRepository,
                               MemberRepository memberRepository,
                               ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAllByStatusWithDetails(ReservationStatus.BOOKING);
    }

    public List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId,
                                                                       LocalDate fromDate, LocalDate toDate) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 Id의 사용자가 없습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당 Id의 테마가 없습니다."));
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, fromDate, toDate);
    }

    public List<Reservation> findAllByMember(Member member) {
        return reservationRepository.findAllByMemberAndStatusWithDetails(member, ReservationStatus.BOOKING);
    }
}
