package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.CreateReservationParam;
import roomescape.application.reservation.dto.ReservationResult;
import roomescape.application.reservation.dto.ReservationSearchParam;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.ReservationException;
import roomescape.infrastructure.error.exception.ReservationTimeException;
import roomescape.infrastructure.error.exception.ThemeException;

@Service
public class ReservationService {

    private final ReservationTimeRepository reservationTImeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(ReservationTimeRepository reservationTImeRepository,
                              ReservationRepository reservationRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              Clock clock) {
        this.reservationTImeRepository = reservationTImeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional
    public Long create(CreateReservationParam createReservationParam) {
        Member member = getMemberById(createReservationParam.memberId());
        ReservationTime reservationTime = getReservationTimeById(createReservationParam.timeId());
        Theme theme = getThemeById(createReservationParam.themeId());
        if (isAlreadyReservedAt(createReservationParam.date(), reservationTime, theme)) {
            throw new ReservationException("날짜와 시간이 중복된 예약이 존재합니다.");
        }
        Reservation reservation = new Reservation(
                member,
                createReservationParam.date(),
                reservationTime,
                theme
        );
        reservation.validateReservable(LocalDateTime.now(clock));
        return reservationRepository.save(reservation).getId();
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(memberId + "에 해당하는 member 튜플이 없습니다."));
    }

    private ReservationTime getReservationTimeById(Long timeId) {
        return reservationTImeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException(timeId + "에 해당하는 reservation_time 튜플이 없습니다."));
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeException(themeId + "에 해당하는 theme 튜플이 없습니다."));
    }

    private boolean isAlreadyReservedAt(LocalDate date, ReservationTime reservationTime, Theme theme) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(date, reservationTime.getId(), theme.getId());
    }

    public void deleteById(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationResult> findAll() {
        List<Reservation> reservations = reservationRepository.findAllWithMemberAndTimeAndTheme();
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public ReservationResult findById(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        return ReservationResult.from(reservation);
    }

    private Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(reservationId + "에 해당하는 reservation 튜플이 없습니다."));
    }

    public List<ReservationResult> findReservationsBy(ReservationSearchParam reservationSearchParam) {
        List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                reservationSearchParam.themeId(),
                reservationSearchParam.memberId(),
                reservationSearchParam.from(),
                reservationSearchParam.to()
        );
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationWithStatusResult> findReservationsWithStatus(Long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationWithStatusResult::from)
                .toList();
    }
}
