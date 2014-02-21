**Recognito : Text Independent Speaker Recognition in Java**
============================================================

## What to expect

While the lib truly is in its very early stage of development, it is already functional : out of 500 speaker voices extracted from Ted.com talks, Recognito identifies them all.

DISCLAIMER : the above doesn't mean anything for real life scenarios, please read on :-)

Indeed, the Ted based test is quite biased : 
- overall good quality recordings
- professional speakers usually speak loud and clear
- vocal samples (both training and identifying ones) were extracted from a single recording session, which means the surrounding noise and the average volume of the voice remains stable

So the vocal print extraction works as advertised but "probably" won't be able to cope with vocal samples of the same speaker coming from various recording systems with huge differences and/or very different sounding environments. 

Please note, I used the word "probably" so basic testing on your side should rapidly provide better insight on whether or not the current state of Recognito is suitable for your particular use case.

There are already people out there quite satisfied with the results... I'm a perfectionist aiming for state-of-the-art technology :-)

## Beyond functionality : the initial goals

The reason why I started this project is that in 2014, AFAICT, there are no Speaker Recognition FOSS available that would meet at least the first 4 criteria of the following list :
- **Available in the form of a library** so you could add this new feature to your app
- **Easy on the user** : short learning curve
- **Fit for usage in a multithreaded environment** (e.g. a web server)
- Using a **permissive licensing model** (I.e. not requiring your app to be OSS as well)
- Keeping an eye on **memory footprint**
- Keeping an eye on **processing efficiency**
- **Written in Java** or a JVM language or providing full JNI hooks

These are mostly software design issues and I wanted to aim at those first before improving on the algorithms. 

## Usage

```
Recognito<String> recognito = new Recognito<>();

VocalPrint print = recognito.createVocalPrint("Elvis", new File("OldInterview.wav"));

// handle persistence the way you want
myUser.setVocalPrint(print);
userDao.saveOrUpdate(myUser);
        
// Now check if the King is back
List<String> matches = recognito.recognize(new File("SomeFatGuy.wav"));
        
System.out.println("Elvis is back : " + matches.get(0).equals("Elvis"));
```

Admittedly, this should be easy enough when you're using files but it's not the whole story. Please check the API for other vocal print extraction methods.
One missing feature that's high on my TODO list is automatic handling of microphone input : automatically stop when the user stops talking or after a predefined delay.
