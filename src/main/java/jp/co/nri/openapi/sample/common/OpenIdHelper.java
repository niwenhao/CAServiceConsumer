package jp.co.nri.openapi.sample.common;

import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public interface OpenIdHelper {
	default String uuidGen() {
		return UUID.randomUUID().toString();
	}
	
	default Claims parseIdToken(byte[] cert, String jwt) {
		Claims res = Jwts.parser().setSigningKey(cert).parseClaimsJws(jwt).getBody();
		return res;
	}
}
