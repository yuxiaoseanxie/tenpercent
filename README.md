adr-appLiveNation
=================

Live Nation 2 for Android, the cause and solution of all Live Nation 2 for Android's problems.

Building
========

To build the Live Nation app you will need to install [Android Studio](http://developer.android.com/sdk/installing/studio.html).

To begin, you will need to clone three repositories onto your computer:

	git clone git@github.com:TeamSidewinder/adr-appLiveNation.git adr-appLiveNation
	git clone git@github.com:TeamSidewinder/adr-libsLabsPlatform.git adr-libsLabsPlatform
	git clone git@github.com:TeamSidewinder/adr-libTicketing.git adr-libTicketing

Once the repositories have been cloned, you will import the adr-appLiveNation project into Android Studio. After import, you will need to install any required SDK components:

Open the Android SDK Manager from Android Studio via the Tools->Android->SDK Manager, and make sure that..

a) Any item with “Update” as its status is selected
b) That the following items are selected: 

	[x] Android 4.4.2 API 19: SDK Platform
	[x] Android 4.0 API 14: SDK Platform (for Facebook SDK)
	[x] Extras: Google Play Services
	[x] Extras: Google Repository

For good luck, select Tools->Android->Sync project with Gradle Files, close and re-open Android Studio, and/or select the File->Synchronize menu option.

Now you need to update your properties to be able to build in debug and releae mode.
Go to Users/username/.gradle
Open the gradle.properties file. If it does not exist create it.

Copy and paste the following variable names and update paths and passwords.

	LNReleaseStoreFile= .../livenation-release-key.keystore
	LNStorePassword=
	LNKeyAlias=livenation_alias
	LNKeyPassword= ...

It will then be possible to build and run the project.


Running UIAutomator tests
=========================

Experimental: UIAutomator tests can be run from the root of the app folder (adr-appLiveNation/) via the command line, eg:

./gradlew uiRun

Will run the UIAutomator tests specified by the 'UiAutomator/build.gradle' file


The end.