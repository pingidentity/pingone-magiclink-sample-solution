package com.pingidentity.pingone.magiclink.controllers;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pingidentity.pingone.magiclink.exceptions.CodeNotFoundException;
import com.pingidentity.pingone.magiclink.exceptions.CodeNotProvidedException;
import com.pingidentity.pingone.magiclink.notf.EmailSender;
import com.pingidentity.pingone.magiclink.otp.IRequestStorage;
import com.pingidentity.pingone.magiclink.otp.OTLRequest;
import com.pingidentity.pingone.magiclink.otp.OneTimeLink;
import com.pingidentity.pingone.magiclink.utils.JwtUtilities;

@RestController
public class PingOneMagicLinkController {
	private static final Logger log = LoggerFactory.getLogger(PingOneMagicLinkController.class);

	private static final String PATH_CLAIMOTP = "/pingone/launch";

	private static final String DEFAULT_HOST_HEADER = "X-FORWARDED-FOR";
	
	@Autowired
	private IRequestStorage requestStorage;

	@Autowired
	private String baseUrl;

	@Autowired
	private EmailSender emailSender;

	@Autowired
	private String pingoneBaseUrl;

	@Autowired
	private String pingoneClientId;

	@Autowired
	private String pingoneClientSecret;

	@Autowired
	private Long otlExpiresMilliseconds;
	
	@Autowired
	private String ipAddressHeader;
	
	@Autowired
	private Boolean isDevMode;

	@RequestMapping(value = PATH_CLAIMOTP, method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public void claimOTP(@RequestParam(name = "code", required = true) String code, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (StringUtils.isEmpty(code))
			throw new CodeNotProvidedException("Code not provided in request");

		if (!requestStorage.isRequestExists(code))
			throw new CodeNotFoundException("Code not provided: " + code);

		String ipAddress = getIpAddress(request);
		
		if (requestStorage.getRequest(code, ipAddress) == null) {
			requestStorage.remove(code);
			throw new CodeNotFoundException("Code expired: " + code);
		}
		
		OTLRequest otlRequest = requestStorage.getRequest(code, ipAddress);
		
		String token = getToken(otlRequest);
		String oidcUrl = String.format("%s/as/authorize?client_id=%s&response_type=code&scope=%s&nonce=%s&code_challenge_method=%s&code_challenge=%s&redirect_uri=%s&state=%s",
				this.pingoneBaseUrl, otlRequest.getClient_id(), otlRequest.getScope(), otlRequest.getNonce(), otlRequest.getCode_challenge_method(), otlRequest.getCode_challenge(), otlRequest.getRedirect_uri(), otlRequest.getState());

		String registrationUrl = String.format("%s&%s=%s", oidcUrl, "login_hint_token", token);

		response.setStatus(302);
		response.setHeader("Location", registrationUrl);

		requestStorage.remove(code);
	}

	private String getIpAddress(HttpServletRequest request) {
		
		if(ipAddressHeader == null)
			ipAddressHeader = DEFAULT_HOST_HEADER;
		
		if(!StringUtils.isEmpty(request.getHeader(ipAddressHeader)))
			return request.getHeader(ipAddressHeader);
		else
			return request.getRemoteAddr();
	}

	@RequestMapping(value = "/pingone/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public OneTimeLink createRegistrationOTPOpenToken(@RequestBody OTLRequest otlRequest, @RequestParam(name = "dev-mode", required = false) boolean requestDevMode, HttpServletRequest request) throws Exception {

		String otl = getOTL(request, otlRequest);
		sendEmail(otlRequest, otl);

		if(this.isDevMode && requestDevMode)
			return OneTimeLink.getInstance("success", otl);
		else
			return OneTimeLink.getInstance("success");

	}

	private String getToken(OTLRequest otlRequest) throws Exception {
		String subject = otlRequest.getSubject();
		
		String aud = this.pingoneBaseUrl + "/as";
		
		if(log.isDebugEnabled())
			log.debug("Signing login_hint_token with: " + this.pingoneClientId + ", " + this.pingoneClientSecret);
		
		return JwtUtilities.getLoginHintToken(this.pingoneClientId, this.pingoneClientSecret, subject, aud);
	}

	private void sendEmail(OTLRequest otlRequest, String otl) throws IOException {

		String htmlText = "<H1>Hello</H1><p>Your magic link: </p><p><a id=\"otlhref\" href=\"" + otl
				+ "\">Click here to log in</a></p>";
		
		emailSender.send(otlRequest.getSubject(), otl);
	}

	private String getOTL(HttpServletRequest request, OTLRequest otlRequest) {

		String key = UUID.randomUUID().toString();
		
		String ipAddress = getIpAddress(request);

		requestStorage.register(key, otlRequest, otlExpiresMilliseconds, ipAddress);

		String otl = String.format("%s%s?code=%s", this.baseUrl, PATH_CLAIMOTP, key);
		
		return otl;
	}
}
