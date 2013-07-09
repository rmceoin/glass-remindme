Glass RemindMe
========================

Use Google Glass to remind you to do things based on your location.

This was a direct copy of the Java Mirror API demo app.  From there
I'm making changes to it to get an understanding of the API.

mvn clean

mvn compile

To push to App Engine:

~/appengine-java-sdk-1.8.1/bin/appcfg.sh --oauth2 update ./web && curl https://glass-remindme.appspot.com/



