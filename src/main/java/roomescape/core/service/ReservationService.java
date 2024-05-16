package roomescape.core.service;

import io.jsonwebtoken.MalformedJwtException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.dto.member.LoginMember;
import roomescape.core.dto.reservation.MyReservationResponse;
import roomescape.core.dto.reservation.ReservationRequest;
import roomescape.core.dto.reservation.ReservationResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse create(final ReservationRequest request) {
        final Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(IllegalArgumentException::new);
        final ReservationTime reservationTime = reservationTimeRepository.findById(request.getTimeId())
                .orElseThrow(IllegalArgumentException::new);
        final Theme theme = themeRepository.findById(request.getThemeId())
                .orElseThrow(IllegalArgumentException::new);
        final Reservation reservation = new Reservation(member, request.getDate(), reservationTime, theme);

        validateDuplicatedReservation(reservation, reservationTime);
        reservation.validateDateAndTime();

        final Reservation savedReservation = reservationRepository.save(reservation);

        return new ReservationResponse(savedReservation.getId(), savedReservation);
    }

    private void validateDuplicatedReservation(final Reservation reservation, final ReservationTime reservationTime) {
        final Integer reservationCount = reservationRepository.countByDateAndTimeAndTheme(
                reservation.getDate(),
                reservationTime, reservation.getTheme());
        if (reservationCount > 0) {
            throw new IllegalArgumentException("해당 시간에 이미 예약 내역이 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findAllByMember(final LoginMember loginMember) {
        final Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new MalformedJwtException("올바르지 않은 접근입니다."));
        return reservationRepository.findAllByMember(member)
                .stream()
                .map(MyReservationResponse::new)
                .toList();
    }

    @Transactional
    public void delete(final long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByMemberAndThemeAndPeriod(final Long memberId, final Long themeId,
                                                                      final String from, final String to) {
        final Member member = memberRepository.findById(memberId).orElseThrow(IllegalArgumentException::new);
        final Theme theme = themeRepository.findById(themeId).orElseThrow(IllegalArgumentException::new);
        final LocalDate dateFrom = LocalDate.parse(from);
        final LocalDate dateTo = LocalDate.parse(to);

        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }
}
