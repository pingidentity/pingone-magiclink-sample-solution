# PingOne Magic Link Android Application

The Android application, in conjunction with the PingOne Magic Link Service provides a passwordless authentication experience into a PingOne application using a One Time Link.

More details about the Magic Link Service can be found [here](../../../).

## Setup and Installation

Configure the gradles.properties file with the following items.

Configuration Name | Description | Example
--- | --- | ---
CLIENT_CALLBACK | The redirect_uri of the OIDC client. It should point to a deep link (custom scheme) configured in the AndroidManifest.xml file. | pingapac://magiclink/callback
CLIENT_ID | Client ID for the mobile application. | {clientId}
CLIENT_SCOPES | Client scopes used during the authorization flow. This will determine what information is available when calling the userinfo service | openid profile
MAGICLINK_BASEURL | Frontend Base URL of the magic link service | https://magiclink.pingapac.com
OIDC_USERINFO | The PingOne userinfo service endpoint | https://auth.pingone.com/{environmentId}/as/userinfo
OIDC_TOKEN | The PingOne userinfo service endpoint | https://auth.pingone.com/{environmentId}/as/token
OIDC_JWKS | The PingOne userinfo service endpoint | https://auth.pingone.com/{environmentId}/as/jwks

Once configured, perform the following:
1. File -> Sync Project with Gradle Files
2. Build -> Rebuild Project
3. Run the application on an Android Virtual Device (AVD).
