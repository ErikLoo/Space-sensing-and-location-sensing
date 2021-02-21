# Detecting-Open-or-Closed-Space-with-Acoustic-Sensing

<p align="center">
  <img src="Images/spaces.png">
</p>

The purpose of this project is to explore the technical feasibility of detecting open/closed space in the indoor environment with acoustic sensing. For the evaluation, We identified 21 spaces, commonly seen in the office or household environments. The first 14 spaces shown in the image are closed space, while the last seven spaces count as open space. Please note that some of the closed space are slights opened to demonstrate the sensor placement. We then placed a self-made prototype in each space. The prototype is capable of emitting and receiving ultrasonic waves (<i>17K-20K Hz sine-wave sweep signal</i>). Since open and closed spaces would produce different reflections of waves, we can train a machine classifier to learn the differences and differentiate the two spaces. The results of this project could inform the future design of an <b>indoor item-finding system for lost objects</b>. 

<b>Hardware Setup - An Android device with an amplified receiver</b>
<p align="center">
  <img src="Images/open_space.png">
</p>

The prototype is made of: (1) speaker (2) microphone (3) android device (4) amplification circuit (5) 9V battery used to power the <a href="http://afrotechmods.com/tutorials/2017/01/17/how-to-make-a-simple-1-watt-audio-amplifier-lm386-based/">amplification circuit</a>.
The android device is essentially being used as programmable micro-controller. We added an extenal speaker-microphone module to the device for two primary reasons: (1) To amplify the acoustic signal. (2) To place the microphone and the speaker on the same side so the body of the phone won't impede the propagation of sound waves. 
<br/>
<br/>
<b>SoQrLocServer - The Java server</b> 
<p align="center">
  <img src="Images/server.JPG">
</p>
The SoQrLocServer is a Java server that runs on PC. The serve is responsbile for processing all the audio files sent from multiple Andorid devices. It can be opened in Eclipse. Once open, click on the "Run" button the IDE and wait for the server to be ready. 
<br/>
<br/>
<b>SoLrSpacePropertyDetection - The TCP client responsbile for operating the android device</b>
<p align="center">
  <img src="Images/client2.JPG">
</p>
SoLrSpacePropertyDetection is the TCP (Transmission Control Protocol) client that runs on the Android device. It's responsible for collecting and sending the audio files throught the WLAN to the Javs server. To run the client, install the client app on a smartphone through Android Studio and connect the smartphone to the WLAN. To connect to the Java server hosted on PC, type in the IPv4 Address and press "Send". 
<br/>
<br/>
<b>AcousticFeaturesExtraction</b> 
<p align="center">
  <img src="Images/closed.png" width = "300px">
  <img src="Images/open.png" width = "300px">
</p>
This module computes MFCC (Mel-frequency cepstral coefficients) from the received audio files and identifies 12 MFCC features. It then use these features to train a classifier (multilayered perceptron) to detect whether the space in which the sensor unit is located is open or closed. Spectrograms of the sound reflections collected in a closed environment (left) and a open environment (right) are presented above.
<br/>
<br/>
<b>Results</b>
<p align="center">
  <img src="Images/accuracy.JPG">
</p>
For each type environment at each time interval, we had the sensor unit probe 100 times, which summed up to a total of 21x100 = 2100 times. All the data were then fed into a multilayered perceptron for classification. The final classification accuracy is presented in the image above. Overall, it seem very technically feasible to detect open/closed space in the indoor environment with acoustic sensing
