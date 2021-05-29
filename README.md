# PingOne Magic Link Service

The PingOne Magic Link Service provides a passwordless authentication experience into a PingOne application using a One Time Link.

The Magic Link service performs the following actions:
1. Captures and temporarily stores an OIDC authorization request
   - A mobile application would send details of the request to this service.
2. Generates a One Time Link and emails it to the user.
3. Once claimed over email, the service generates an OIDC authorization url with a signed login_hint_token containing the email as the subject before redirecting the browser over a 302 response.
4. PingOne validates the login_hint_token and automatically logs the user in. An OIDC code or token is sent back to the redirect uri sent in step 1.
   - The flow will return to the mobile application if the redirect_uri is a mobile deep link.

## Accompanying Android Application

This service comes with a sample Android Application, found [here](mobile-android).

## Sequence Diagram

This github project peforms the part of the "Magic Link Service" in the following diagram:

<img src="artefacts/uml_magiclink.png">

## PingOne Pre-requisites

The solution requires a MFA Only Authentication Policy, and an OIDC Client.

### MFA Only Authentication Policy

Create an MFA Only Policy with the following attributes:
- NONE OR INCOMPATIBLE METHODS: BYPASS

### OIDC Client

Create an OIDC Client with the following attributes:
- A generated Client Secret.
- Response Type: Code
- Grant Type: Authorization Code
   - PKCE Enforcement: S256_REQUIRED
- Token Endpoint Authentication Method: NONE
- Resources: add profile
- Policies: Select the MFA Only Policy you created above.

### Sample Users

This solution requires that the users entered into the mobile app exist with the following attributes:
- Username: email address of the user.
- MFA Enabled: true.

## Setup and Installation

You can launch the Magic Link Service using two methods:
- Java with packaged maven project
- Docker

The following configuration applies:

Configuration Name | Docker Env Name | Description | Example
--- | --- | --- | ---
base.url | MAGICLINK_base_url | Frontend Base URL of the magic link service | https://magiclink.pingapac.com
otl.expire.ms | MAGICLINK_otl_expire_ms | One time link expiry in milliseconds | 30000
pingone.base.url | MAGICLINK_pingone_base_url | Auth Base URL of the PingOne environment | https://auth.pingone.com/{environmentId}
pingone.client.id | MAGICLINK_pingone_client_id | Client ID for the mobile application. | {clientId}
pingone.client.secret | MAGICLINK_pingone_client_secret | Client Secret for the OIDC Client. | {clientSecret}
mail.smtp.from | MAGICLINK_mail_smtp_from | Mail from address | noreply@mycompany.com
mail.smtp.username | MAGICLINK_smtp_username | Username to authenticate into the SMTP server | smtpuser@mycompany.com
mail.smtp.password | MAGICLINK_smtp_password | Password to authenticate into the SMTP server | ******
mail.smtp.starttls.enable | MAGICLINK_smtp_starttls_enable | Require TLS mode | true/false
mail.smtp.host | MAGICLINK_smtp_host | SMTP hostname | smtp.gmail.com
mail.smtp.port | MAGICLINK_smtp_port | SMTP port | 587
devmode | MAGICLINK_devmode | Dev mode returns the OTL during the OIDC claim dropoff. This isn't recommended in production because the OTL should only be received via Email. It is useful for automated testing e.g. via Postman | true/false


### Launch with Java

The Java project needs to be launched at a network destination resolvable by the mobile application.

1. Navigate to where the magic-link-webservice/pom.xml exists in terminal (root folder of this project).
2. Instantiate src/main/resources/application.properties from src/main/resources/application.properties.template
3. Configure the application.properties
4. Package the Maven project: mvn install
5. Run the target: java -jar target/magiclink-0.0.1-SNAPSHOT.war

### Launch with Docker

The Docker container needs to be launched at a network destination resolvable by the mobile application.

1. Navigate to where the Dockerfile exists in terminal (root folder of this project).
2. Build the docker image:
   - docker build . -t {mydocker}/magiclink
3. Run the docker image:
```
docker run -p 9191:9191 \
  --env MAGICLINK_base_url=http://localhost:9191 \
  --env MAGICLINK_pingone_base_url=https://auth.pingone.com/xxxxxxxxxxx \
  --env MAGICLINK_pingone_client_id=xxxxxxxxxxx \
  --env MAGICLINK_pingone_client_secret=xxxxxxxxxxx \
  --env MAGICLINK_mail_smtp_from=noreply@mycompany.com \
  --env MAGICLINK_mail_smtp_username=smtp@mycompany.com \
  --env MAGICLINK_mail_smtp_password="mypassword" \
  --env MAGICLINK_mail_smtp_auth=true \
  --env MAGICLINK_mail_smtp_starttls_enable=true \
  --env MAGICLINK_mail_smtp_host=smtp.gmail.com \
  --env MAGICLINK_mail_smtp_port=587 \
  {mydocker}/magiclink:latest
```

## Test with Postman

Postman scripts [here](artefacts/postman.collection) have been required to test the solution out without a mobile application.

Import the collection into postman and modify the following Collection Variables:

Configuration Name | Description | Example
--- | --- | ---
magiclink-baseurl | Frontend Base URL of the magic link service | https://magiclink.pingapac.com
magiclink-subject | Email address of the subject. User must exist in PingOne with username=email | bob@mailinator.com
pingone-baseurl | Auth Base URL of the PingOne environment | https://auth.pingone.com/{environmentId}
pingone-client_id | Client ID for the mobile application. | {clientId}
pingone-client_redirect_uri | Redirect URI configured in the client. | pingapac://magiclink/callback

Run the Postman steps in sequence.
