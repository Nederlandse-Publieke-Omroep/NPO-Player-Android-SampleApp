# NPO Player library Sample Application for Android

## SampleApp:

To get the sample app to run and play videos make sure to add you Bitmovin player license key,
NPOTag cloud package key and NPO Player Token signing configuration (Store file for signing release
builds is optional to fill in) to your [local.properties](/local.properties) file. Add the following
properties with the correct values:

```text
# The following settings need to be set and filled in to run the Sample App correctly
npotag_package_cloud_key=
your_player_license_key_here=
your_analytics_license_key_here=
# For non-NPO Start developers: You can set the same issuer and signature for `start` and `plus`. You just might not be able to play everything depending on your issuer restrictions.
token_issuer_start=
token_issuer_plus=
token_signature_start_dev=
token_signature_plus_dev=
token_signature_start_acc=
token_signature_plus_acc=
token_signature_start_prod=
token_signature_plus_prod=
# The following settings need to be set, but can remain empty if you don't want to create release builds. 
storeFile=
storePassword=
keyAlias=
keyPassword=
```

**Note:**

The NPOTag package cloud key can be requested from the DIAZ team. For more info
see https://npotag.npo-data.nl/docs/index.html

The Bitmovin license and analytics keys and the issuer token and signature can be requested
from [player@npo.nl](mailto:player@npo.nl).  
