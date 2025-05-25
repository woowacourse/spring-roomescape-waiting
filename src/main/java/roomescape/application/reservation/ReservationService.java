package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.CreateReservationParam;
import roomescape.application.reservation.dto.ReservationResult;
import roomescape.application.reservation.dto.ReservationSearchParam;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ThemeSchedule;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ThemeScheduleReader themeScheduleReader;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ThemeScheduleReader themeScheduleReader,
                              MemberRepository memberRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.themeScheduleReader = themeScheduleReader;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional
    public Long create(CreateReservationParam createReservationParam) {
        Member member = getMemberById(createReservationParam.memberId());
        ThemeSchedule themeSchedule = themeScheduleReader.getThemeSchedule(
                createReservationParam.date(),
                createReservationParam.themeId(),
                createReservationParam.timeId()
        );
        if (isAlreadyReservedAt(themeSchedule)) {
            throw new BusinessRuleViolationException("날짜와 시간이 중복된 예약이 존재합니다.");
        }
        Reservation reservation = Reservation.create(member, themeSchedule);
        reservation.validateReservable(LocalDateTime.now(clock));
        return reservationRepository.save(reservation).getId();
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

    public List<ReservationResult> findReservationsBy(ReservationSearchParam reservationSearchParam) {
        List<Reservation> reservations = reservationRepository.findByThemeScheduleThemeIdAndMemberIdAndThemeScheduleDateBetween(
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

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundEntityException(memberId + "에 해당하는 member 튜플이 없습니다."));
    }

    private boolean isAlreadyReservedAt(ThemeSchedule themeSchedule) {
        return reservationRepository.existsByThemeSchedule(themeSchedule);
    }

    private Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundEntityException(reservationId + "에 해당하는 reservation 튜플이 없습니다."));
    }
}
