package roomescape.member.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.request.CreateReservationRequest;
import roomescape.member.dto.response.CreateReservationResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class AdminService {
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public AdminService(final MemberRepository memberRepository,
                        final ReservationTimeRepository reservationTimeRepository,
                        final ThemeRepository themeRepository,
                        final ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public CreateReservationResponse createReservation(final CreateReservationRequest createReservationRequest) {
        Member member = findMember(createReservationRequest.memberId());
        Theme theme = findTheme(createReservationRequest.themeId());
        ReservationTime reservationTime = findReservationTime(createReservationRequest.timeId());

        Reservation reservation = reservationRepository.save(
                new Reservation(null, member, createReservationRequest.date(), reservationTime, theme));
        return CreateReservationResponse.from(reservation);
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    private ReservationTime findReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }
}
