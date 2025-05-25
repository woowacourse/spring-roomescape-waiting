package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.ThemeSchedule;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;

@Service
public class ReservationService {

    private final ReservationTimeRepository reservationTImeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final Clock clock;

    public ReservationService(ReservationTimeRepository reservationTImeRepository,
                              ReservationRepository reservationRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository, WaitingRepository waitingRepository,
                              Clock clock) {
        this.reservationTImeRepository = reservationTImeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
        this.clock = clock;
    }

    @Transactional
    public Long create(CreateReservationParam createReservationParam) {
        Member member = getMemberById(createReservationParam.memberId());
        ReservationTime reservationTime = getReservationTimeById(createReservationParam.timeId());
        Theme theme = getThemeById(createReservationParam.themeId());
        if (isAlreadyReservedAt(createReservationParam.date(), reservationTime, theme)) {
            throw new BusinessRuleViolationException("날짜와 시간이 중복된 예약이 존재합니다.");
        }
        Reservation reservation = Reservation.create(
                member,
                createReservationParam.date(),
                reservationTime,
                theme
        );
        reservation.validateReservable(LocalDateTime.now(clock));
        return reservationRepository.save(reservation).getId();
    }

    @Transactional
    public void deleteById(Long reservationId) {
        Optional<Waiting> findWaiting = findTopStartedWaiting(reservationId);
        reservationRepository.deleteById(reservationId);
        findWaiting.ifPresent(waiting -> {
            Reservation reservation = waiting.toReservation();
            reservationRepository.save(reservation);
            waitingRepository.delete(waiting);
        });
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

    private Optional<Waiting> findTopStartedWaiting(Long reservationId) {
        return reservationRepository.findThemeScheduleById(reservationId)
                .flatMap(waitingRepository::findTopByThemeScheduleOrderBystartedAt);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundEntityException(memberId + "에 해당하는 member 튜플이 없습니다."));
    }

    private ReservationTime getReservationTimeById(Long timeId) {
        return reservationTImeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundEntityException(timeId + "에 해당하는 reservation_time 튜플이 없습니다."));
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundEntityException(themeId + "에 해당하는 theme 튜플이 없습니다."));
    }

    private boolean isAlreadyReservedAt(LocalDate date, ReservationTime reservationTime, Theme theme) {
        return reservationRepository.existsByThemeSchedule(new ThemeSchedule(date, reservationTime, theme));
    }

    private Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundEntityException(reservationId + "에 해당하는 reservation 튜플이 없습니다."));
    }
}
