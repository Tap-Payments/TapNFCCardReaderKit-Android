# TapNFCCardReaderKit-Android
A SDK that provides an interface to scan cards using NFC.

[![Platform](https://img.shields.io/badge/platform-Android-inactive.svg?style=flat)](https://github.com/Tap-Payments/TapNFCCardReaderKit-Android.git)
[![SDK Version](https://img.shields.io/badge/minSdkVersion-19-blue.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)
[![SDK Version](https://img.shields.io/badge/targetSdkVersion-29-informational.svg)](https://stuff.mit.edu/afs/sipb/project/android/docs/reference/packages.html)
## Requirements

To use the SDK the following requirements must be met:

1. **Android Studio 3.6** or newer
2. **Android SDK Tools 29.0.0 ** or newer
3. **Android Platform Version: API 29: Android 10.0 (Q)
4. **Android targetSdkVersion: 29

# Installation
---
<a name="include_library_to_code_locally"></a>
### Include goSellSDK library as a dependency module in your project
---
1. Clone goSellSDK library from Tap repository
   ```
       git@github.com:Tap-Payments/TapNFCCardReaderKit-Android.git
    ```
2. Add goSellSDK library to your project settings.gradle file as following
    ```java
        include ':library', ':YourAppName'
    ```
3. Setup your project to include goSellSDK as a dependency Module.
   1. File -> Project Structure -> Modules -> << your project name >>
   2. Dependencies -> click on **+** icon in the screen bottom -> add Module Dependency
   3. select goSellSDK library

<a name="installation_with_jitpack"></a>
### Installation with JitPack
---
[JitPack](https://jitpack.io/) is a novel package repository for JVM and Android projects. It builds Git projects on demand and provides you with ready-to-use artifacts (jar, aar).

To integrate goSellSDK into your project add it in your **root** `build.gradle` at the end of repositories:
```java
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```java
	dependencies {

           implementation "io.reactivex.rxjava2:rxjava:2.2.19"
           implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
	}
```
## Setup
### Configure TapNfcCardReader
TapNfcCardReader reads the data from the card and process it internally.
To configure the TapNfcCardReader from the activity

1.Declare the TapNfcCardReader
```
private TapNfcCardReader tapNfcCardReader;
```
2.Initialize the tapNfcCardReader
```
tapNfcCardReader = new TapNfcCardReader(this);
```
3.In OnResume of the activity use as below:

i. **TapNfcUtils.isNfcAvailable** to check if the device supports NFC.

ii. **TapNfcUtils.isNfcEnabled** to check if NFC is enabled.

iii. **tapNfcCardReader.enableDispatch()** Activates NFC using TapNfcCardReader to read NFC Card details.
```
 @Override
    protected void onResume() {
        if (TapNfcUtils.isNfcAvailable(this)) {
            if (TapNfcUtils.isNfcEnabled(this)) {
                tapNfcCardReader.enableDispatch();
                scancardContent.setVisibility(View.VISIBLE);
            } else
                enableNFC();
        } else {
            scancardContent.setVisibility(View.GONE);
            cardreadContent.setVisibility(View.GONE);
            noNfcText.setVisibility(View.VISIBLE);
        }
        super.onResume();
 ```
 4. On launch onNewIntent() method:

i. **tapNfcCardReader.isSuitableIntent(intent)** checks if the intent or scanned data is good readable

ii. Declare  **Disposable**
 ```
  private Disposable cardReadDisposable = Disposables.empty();
  ```
 iii. Read the scanned data and assign to fields of your choice
 ```
   cardReadDisposable = tapNfcCardReader
                    .readCardRx2(intent)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::showCardInfo,
                            throwable -> displayError(throwable.getMessage()));
```
iv.Here its assigned to showCardInfo using **TapEmvCard** as below:
```
 private void showCardInfo(TapEmvCard emvCard) {
        String text = TextUtils.join("\n", new Object[]{
                TapCardUtils.formatCardNumber(emvCard.getCardNumber(), emvCard.getType()),
                DateFormat.format("M/y", emvCard.getExpireDate()),
                "---",
                "Bank info (probably): ",
                emvCard.getAtrDescription(),
                "---",
                emvCard.toString().replace(", ", ",\n")
        });
        Log.e("showCardInfo:", text);
        scancardContent.setVisibility(View.GONE);
        cardreadContent.setVisibility(View.VISIBLE);
        cardnumberText.setText(emvCard.getCardNumber());
        expiredateText.setText(DateFormat.format("M/y", emvCard.getExpireDate()));
        cardType.setText(emvCard.getApplicationLabel());
        mProgressDialog.dismiss();
    }
```