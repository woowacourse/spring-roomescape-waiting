package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.*;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationPolicy reservationPolicy;

    public ReservationService(final ReservationRepository reservationRepository,
                              final MemberRepository memberRepository, final ThemeRepository themeRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              ReservationPolicy reservationPolicy) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationPolicy = reservationPolicy;
    }

    public ReservationResult create(CreateReservationParam createReservationParam) {
        ReservationTime reservationTime = reservationTimeRepository.findById(createReservationParam.timeId())
                .orElseThrow(() -> new NotFoundReservationTimeException(createReservationParam.timeId() + "에 해당하는 정보가 없습니다."));
        Theme theme = themeRepository.findById(createReservationParam.themeId())
                .orElseThrow(() -> new NotFoundThemeException(createReservationParam.themeId() + "에 해당하는 정보가 없습니다."));
        Member member = memberRepository.findById(createReservationParam.memberId())
                .orElseThrow(() -> new NotFoundMemberException(createReservationParam.memberId() + "에 해당하는 정보가 없습니다."));

        Reservation reservation = Reservation.createNew(member, createReservationParam.date(), reservationTime, theme);
        validateCanReservation(createReservationParam, reservation);

        reservationRepository.save(reservation);
        return ReservationResult.from(reservation);
    }

    private void validateCanReservation(CreateReservationParam param, Reservation reservation) {
        boolean existsDuplicateReservation = reservationRepository.existsDuplicateReservation(
                reservation.getDate(), param.timeId(), param.themeId());
        reservationPolicy.validateReservationAvailable(reservation, existsDuplicateReservation);
    }

    public void deleteById(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public ReservationResult getById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundReservationException(reservationId + "에 해당하는 reservation 튜플이 없습니다."));
        return ReservationResult.from(reservation);
    }

    public List<ReservationResult> getReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findReservationsInConditions(memberId, themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> getMemberReservationsById(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        return ReservationResult.from(reservations);
    }

}
