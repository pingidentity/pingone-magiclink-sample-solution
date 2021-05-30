package com.pingidentity.pingone.magiclink.otp;

public class OTLRequest {
	private String subject;
	private String givenName;
	private String surname;
	private String organisation;
	private String organisation_type;
	private String email;
	private String mobile;
	private String nonce;
	private String state;
	private String client_id;
	private String scope;
	private String redirect_uri;
	private String code_challenge;
	private String code_challenge_method;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getCode_challenge() {
		return code_challenge;
	}
	public void setCode_challenge(String code_challenge) {
		this.code_challenge = code_challenge;
	}
	public String getCode_challenge_method() {
		return code_challenge_method;
	}
	public void setCode_challenge_method(String code_challenge_method) {
		this.code_challenge_method = code_challenge_method;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}
	public void setOrganisation_type(String organisation_type) {
		this.organisation_type = organisation_type;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getSubject() {
		return subject;
	}
	public String getGivenName() {
		return givenName;
	}
	public String getSurname() {
		return surname;
	}
	public String getOrganisation() {
		return organisation;
	}
	public String getOrganisation_type() {
		return organisation_type;
	}
	public String getEmail() {
		return email;
	}
	public String getMobile() {
		return mobile;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public String getRedirect_uri() {
		return redirect_uri;
	}
	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
}
