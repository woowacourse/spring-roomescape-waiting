package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.common.util.time.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.presentation.dto.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.presentation.dto.ThemeResponse;

@Service
public class ReservationService {

    private final DateTime dateTime;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            final DateTime dateTime,
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        this.dateTime = dateTime;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId());
        Theme theme = themeRepository.findById(request.themeId());
        Member member = memberRepository.findById(memberId);

        validateExistDuplicateReservation(request, time);

        Reservation reservation = Reservation.createWithoutId(request.date(), time, theme, member);
        validateCanReserveDateTime(reservation, dateTime.now());
        Long id = reservationRepository.save(reservation);

        return ReservationResponse.from(reservation.assignId(id));
    }

    private void validateExistDuplicateReservation(final ReservationRequest request, final ReservationTime time) {
        if (reservationRepository.existBy(request.themeId(), request.date(), time.getStartAt())) {
            throw new ReservationException("이미 예약이 존재합니다.");
        }
    }

    private void validateCanReserveDateTime(final Reservation reservation, final LocalDateTime now) {
        if (reservation.isCanReserveDateTime(now)) {
            throw new ReservationException("예약할 수 없는 날짜와 시간입니다.");
        }
    }

    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservationById(final Long id) {
        boolean isDeleted = reservationRepository.deleteById(id);
        validateExistIdToDelete(isDeleted);
    }

    private void validateExistIdToDelete(boolean isDeleted) {
        if (!isDeleted) {
            throw new IllegalArgumentException("존재하지 않는 예약입니다.");
        }
    }

    public List<ReservationResponse> searchReservationWithCondition(final SearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findBy(
                condition.memberId(), condition.themeId(),
                condition.dateFrom(), condition.dateTo()
        );

        return reservations.stream()
                .map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        reservation.getDate(),
                        new ReservationTimeResponse(reservation.getTime().getId(), reservation.getTime().getStartAt()),
                        new ThemeResponse(reservation.getTheme().getId(), reservation.getTheme().getName(), reservation.getTheme().getDescription(), reservation.getTheme().getThumbnail()),
                        new MemberResponse(reservation.getMember().getId(), reservation.getMember().getName())
                ))
                .toList();
    }
}
