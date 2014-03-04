The Google API’s within the app require an API key operate. This APIKey is tied to the SHA1 signature of the certificate that signs the app. 

For development, this implies each developer needs to register their own API keys for their own local debug certificates, and then change the app manifest to use these keys (EUGH) 

!!!OR!!!

You can copy/overwrite the debug.keystore file contained within here to your:

~/.android/debug.keystore 

…folder. Your local debug build will now be signed with the same signature that the API keys are configured for. 

