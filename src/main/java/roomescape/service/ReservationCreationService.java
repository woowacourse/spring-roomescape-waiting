package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPolicy;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnAvailableReservationException;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

@Service
@Transactional(readOnly = true)
public class ReservationCreationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationPolicy reservationPolicy;

    public ReservationCreationService(ReservationRepository reservationRepository, MemberRepository memberRepository,
                                      ThemeRepository themeRepository,
                                      ReservationTimeRepository reservationTimeRepository,
                                      ReservationPolicy reservationPolicy) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationPolicy = reservationPolicy;
    }

    @Transactional
    public ReservationResult create(CreateReservationParam param) {
        ReservationComponents components = loadContext(param);
        Reservation reservation = Reservation.createNew(
                components.member, param.date(), components.reservationTime, components.theme);
        validateCanReservation(reservation);
        reservationRepository.save(reservation);

        return ReservationResult.from(reservation);
    }

    private void validateCanReservation(Reservation reservation) {
        boolean existsDuplicateReservation = reservationRepository.existsDuplicateReservation(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        reservationPolicy.validateReservationAvailable(reservation, existsDuplicateReservation);
    }

    @Transactional
    public ReservationResult createWaiting(CreateReservationParam param) {
        ReservationComponents components = loadContext(param);
        Reservation reservation = Reservation.createWaiting(
                components.member, param.date(), components.reservationTime, components.theme);
        validateCanWaiting(reservation);
        reservationRepository.save(reservation);

        return ReservationResult.from(reservation);
    }

    private void validateCanWaiting(Reservation reservation) {
        boolean exists = reservationRepository.hasAlreadyReservedOrWaited
                (reservation.getMember().getId(),
                reservation.getTheme().getId(),
                reservation.getTime().getId(),
                reservation.getDate());
        if (exists) {
            throw new UnAvailableReservationException("이미 동일한 시간에 예약(또는 대기)이 존재합니다.");
        }
    }

    private ReservationComponents loadContext(CreateReservationParam param) {
        ReservationTime reservationTime = reservationTimeRepository.findById(param.timeId())
                .orElseThrow(() -> new NotFoundException("timeId", param.timeId()));
        Theme theme = themeRepository.findById(param.themeId())
                .orElseThrow(() -> new NotFoundException("themeId", param.themeId()));
        Member member = memberRepository.findById(param.memberId())
                .orElseThrow(() -> new NotFoundException("memberId", param.memberId()));

        return new ReservationComponents(member, theme, reservationTime);
    }

    private record ReservationComponents(Member member, Theme theme, ReservationTime reservationTime) {
    }
}
