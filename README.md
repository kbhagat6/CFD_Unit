# Android App for Counterfeit Drug Detection System.
> This app contains the functionalities to directly interface and communicate with a custom-built Counterfeit Drug Detection System. The steps consist of calibration of the spectrometer, the motor controlled sliders that hold the low and high pass filters, and powering the 10 mW laser diode. The main option is an automated process of firing the laser through the beam splitter and low and high pass filter into the spectrometer and then logging the results onto the Android app which notifies the user of the drug's authenticity. 


**Build status:** master ![](https://api.travis-ci.org/owncloud/android.svg?branch=master) stable ![](https://api.travis-ci.org/owncloud/android.svg?branch=stable)
  
  
  The backend software was written on the Arduino Mega 2560 which controlled the heatsink, laser, and the filtering assembly.  Because the laser is activated by the diode driver, the Mega triggers the relay that controls it and writes to the digital pin. In addition, an Arduino thread is being used in the code to read the temperature of the diode every 10 milliseconds and will launch the cooling system in the background whenever the temperature goes above the acceptable threshold. Because the cooler consists of the TEC and heat dissipating fan, two digital out pins would be used to drive the two relays. Lastly, during and after the scanning process, the log messages and spectral data would get sent back to the Smart Phone. The Bluetooth Adafruit API in the Arduino code was used to allow this functionality by allowing connection through a UART service. The spectral data had to be sent to the smart phone in packets of 20 bytes and because the spectral data contained unsigned 16 bit integers, data was sent 10 elements at a time. Despite this, there was only a slight 2 second delay in the whole data transfer. 
  
  The android smart phone app contains a main title page with options of pairing to BLE or viewing the database. The pairing BLE phase consisted of a UART service which connects to a Generic Attribute Server and this allows the UART and the smart phone to pair using a specified UUID. In addition, there were also several callback functions and broadcast sender. The Android activities that were created had to start their own service instance and a broadcast receiver was written which would trigger the callback functions which contains the data that was ready to be read. The Scanning Activity shown in Figure 1, consisted of the buttons to start the scanning spectrum process and a calibration button to get data from the “no filter” option, as well as a checkbox to ensure that the pill was placed in the container where the laser would hit the drug. There was also an advanced calibration GUI shown in Figure 2 which had buttons for activating the laser and heat sink, as well as option to move the slider position of the filter assembly. The database activity shown in Figure 3 contained all the drugs with the corresponding spectral response values and an option to compare the last scanned drug to one in the database. The database used was SQLite, an embedded android database API which stores the database in internal memory. A lower level class was created to extend the Sqlitehelper API and add functionality for creating the database and upgrading. A higher level class was also created which contained the insert, update, and delete queries, which were wrapped in separate functions to be called from the main viewing class. The viewing class contained the algorithm, an element by element voting scheme, where a count of number of elements whose difference was no less than 10 from the spectrum in the database and dividing that by the total number of elements. If the result was larger than 80%, then a “toast message” would be visible, showing the output of the whether the drug matched or not. 

<br>

![](https://raw.githubusercontent.com/kbhagat6/CFD_Unit/master/mainfig.png)
 
## Installation
* <a href="https://github.com/kbhagat6/CFD_Unit/raw/master/app-debug-unaligned.apk" target="_blank">Download the CDF_Unit APK</a>
* To run the python code for the analysis, install <a href="https://docs.continuum.io/anaconda/install"> Anaconda Package Manager tools </a> and set system paths appropriately to enable dependencies. The code will wait for connection to the arduino and will collect realtime data as the spectrometer captures the spectrum deflected off the drug. Graphs will display after each trial. 


Linux, OSX, Windows
```sh
Create the repo folder. 
git clone https://github.com/kbhagat6/CFD_Unit
```


## Gradle.build
Your grade file  /apps folder should match this: 
```
android {
    compileSdkVersion 18 //22
    buildToolsVersion "22.0.1" // "19.1.0"

    defaultConfig {
        applicationId "com.example.krishan.cfd_unit"
        minSdkVersion 18  //21
        targetSdkVersion 18 //22
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:support-v4:22.2.1'
}'''
```



* Arduino System level code can be found <a href=https://bitbucket.org/kbhagat6/truemed-firmware> here </a>









