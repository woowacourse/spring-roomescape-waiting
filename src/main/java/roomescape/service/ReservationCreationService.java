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
import roomescape.service.param.CreateBookingParam;
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
    public ReservationResult create(CreateBookingParam param) {
        ReservationComponents components = loadComponents(param);
        Reservation reservation = Reservation.create(
                components.member, param.date(), components.time, components.theme);
        validateCanReservation(reservation);
        reservationRepository.save(reservation);

        return ReservationResult.from(reservation);
    }

    private void validateCanReservation(Reservation reservation) {
        boolean existsDuplicateReservation = reservationRepository.existsDuplicateReservation(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        reservationPolicy.validateReservationAvailable(reservation, existsDuplicateReservation);
    }

    private ReservationComponents loadComponents(CreateBookingParam param) {
        ReservationTime reservationTime = reservationTimeRepository.findById(param.timeId())
                .orElseThrow(() -> new NotFoundException("timeId", param.timeId()));
        Theme theme = themeRepository.findById(param.themeId())
                .orElseThrow(() -> new NotFoundException("themeId", param.themeId()));
        Member member = memberRepository.findById(param.memberId())
                .orElseThrow(() -> new NotFoundException("memberId", param.memberId()));

        return new ReservationComponents(member, theme, reservationTime);
    }

    private record ReservationComponents(Member member, Theme theme, ReservationTime time) {
    }
}
