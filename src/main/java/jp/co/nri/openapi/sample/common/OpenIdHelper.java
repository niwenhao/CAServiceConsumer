package jp.co.nri.openapi.sample.common;

import java.beans.Beans;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.util.BeanUtil;

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
	
	default String base64UrlHelfSha256(String code) {
		byte[] codeBuf = code.getBytes(StandardCharsets.UTF_8);
		byte[] shaBuf = DigestUtils.sha256(codeBuf);
		byte[] helfShaBuf = new byte[shaBuf.length/2];
		System.arraycopy(shaBuf, 0, helfShaBuf, 0, helfShaBuf.length);
		String rst = Base64.getEncoder().encodeToString(helfShaBuf);
		return rst.replaceAll("\\+", "-").replaceAll("/", "_").replaceAll("=*$", "");
	}
}
