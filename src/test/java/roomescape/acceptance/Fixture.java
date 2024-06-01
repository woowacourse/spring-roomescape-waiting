package roomescape.acceptance;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static roomescape.PreInsertedData.ADMIN;
import static roomescape.PreInsertedData.CUSTOMER_1;
import static roomescape.PreInsertedData.CUSTOMER_2;
import static roomescape.PreInsertedData.CUSTOMER_3;

public class Fixture {

    public static final String secretKey = "pumExiFixehC65FymaHwN/FDue8U48AORoZFXkzoM7E=";

    public static final String adminToken = Jwts.builder()
            .subject(ADMIN.getId().toString())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();

    public static final String customer1Token = Jwts.builder()
            .subject(CUSTOMER_1.getId().toString())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();

    public static final String customer2Token = Jwts.builder()
            .subject(CUSTOMER_2.getId().toString())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();

    public static final String customer3Token = Jwts.builder()
            .subject(CUSTOMER_3.getId().toString())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();
}
