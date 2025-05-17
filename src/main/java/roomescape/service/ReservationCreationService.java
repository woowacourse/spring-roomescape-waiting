package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPolicy;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.NotFoundMemberException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.exception.NotFoundThemeException;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

@Service
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

    public ReservationResult createWaiting(CreateReservationParam param) {
        ReservationComponents components = loadContext(param);
        Reservation reservation = Reservation.createWaiting(
                components.member, param.date(), components.reservationTime, components.theme);
        validateCanWaiting(reservation);
        reservationRepository.save(reservation);

        return ReservationResult.from(reservation);
    }

    private void validateCanWaiting(Reservation reservation) {
        //TODO: validate 같은 사용자가 같은 예약대기를 또 할 수는 없음 / 이미 예약상태의 자신의예약이존재하는데 대기를걸수없음
    }

    private ReservationComponents loadContext(CreateReservationParam param) {
        ReservationTime reservationTime = reservationTimeRepository.findById(param.timeId())
                .orElseThrow(() -> new NotFoundReservationTimeException(param.timeId() + "에 해당하는 정보가 없습니다."));
        Theme theme = themeRepository.findById(param.themeId())
                .orElseThrow(() -> new NotFoundThemeException(param.themeId() + "에 해당하는 정보가 없습니다."));
        Member member = memberRepository.findById(param.memberId())
                .orElseThrow(() -> new NotFoundMemberException(param.memberId() + "에 해당하는 정보가 없습니다."));

        return new ReservationComponents(member, theme, reservationTime);
    }

    private record ReservationComponents(Member member, Theme theme, ReservationTime reservationTime) {
    }
}
