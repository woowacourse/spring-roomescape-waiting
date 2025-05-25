package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.auth.service.dto.LoginMember;
import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDateTime;

@Service
public class CreateReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public CreateReservationService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse create(final ReservationCreateRequest request) {
        if (isAlreadyBooked(request)) {
            throw new AlreadyInUseException("중복되는 예약이 존재합니다.");
        }

        Reservation reservation = createReservation(request);
        validateReservationDateTime(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    private boolean isAlreadyBooked(final ReservationCreateRequest request) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId()
        );
    }

    private Reservation createReservation(final ReservationCreateRequest request) {
        ReservationTime reservationTime = getReservationTime(request);
        Theme theme = getTheme(request);
        LoginMember loginMember = request.loginMember();
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 회원입니다."));
        return new Reservation(member, request.date(), reservationTime, theme);
    }

    private Theme getTheme(final ReservationCreateRequest request) {
        Long themeId = request.themeId();
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
    }

    private ReservationTime getReservationTime(final ReservationCreateRequest request) {
        Long timeId = request.timeId();
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약 가능 시간입니다."));
    }

    private void validateReservationDateTime(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        if (reservation.isBefore(now)) {
            throw new IllegalArgumentException("과거 날짜의 예약은 생성할 수 없습니다.");
        }
    }
}
