package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.reservation.dto.CreateReservationParam;
import roomescape.application.reservation.dto.ReservationResult;
import roomescape.application.reservation.dto.ReservationSearchParam;
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

    public Long create(CreateReservationParam createReservationParam) {
        Member member = memberRepository.findById(createReservationParam.memberId())
                .orElseThrow(() -> new NotFoundEntityException(
                        createReservationParam.memberId() + "에 해당하는 member 튜플이 없습니다."));
        ReservationTime reservationTime = reservationTImeRepository.findById(createReservationParam.timeId())
                .orElseThrow(
                        () -> new NotFoundEntityException(
                                createReservationParam.timeId() + "에 해당하는 reservation_time 튜플이 없습니다."));
        Theme theme = themeRepository.findById(createReservationParam.themeId())
                .orElseThrow(() -> new NotFoundEntityException(
                        createReservationParam.themeId() + "에 해당하는 theme 튜플이 없습니다."));
        if (reservationRepository.existsByDateAndTimeId(createReservationParam.date(), reservationTime.getId())) {
            throw new BusinessRuleViolationException("날짜와 시간이 중복된 예약이 존재합니다.");
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

    public void deleteById(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationResult> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public ReservationResult findById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundEntityException(reservationId + "에 해당하는 reservation 튜플이 없습니다."));
        return ReservationResult.from(reservation);
    }

    public List<ReservationResult> findReservationsBy(ReservationSearchParam reservationSearchParam) {
        List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                reservationSearchParam.themeId(),
                reservationSearchParam.memberId(), reservationSearchParam.from(), reservationSearchParam.to());
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }
}
