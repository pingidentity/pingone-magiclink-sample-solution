# PingOne Magic Link Solution - Web Service

The PingOne Magic Link solution [here](../../../) requires this web service which sends an email one time link for passwordless authentication.

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
