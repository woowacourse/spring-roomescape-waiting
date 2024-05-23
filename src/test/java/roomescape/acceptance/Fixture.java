package roomescape.acceptance;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_ADMIN;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_CUSTOMER_1;

public class Fixture {

    public static final String secretKey = "pumExiFixehC65FymaHwN/FDue8U48AORoZFXkzoM7E=";

    public static final String adminToken = Jwts.builder()
            .subject(PRE_INSERTED_ADMIN.getId().toString())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();

    public static final String customerToken = Jwts.builder()
            .subject(PRE_INSERTED_CUSTOMER_1.getId().toString())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();
}
