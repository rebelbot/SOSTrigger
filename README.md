SOS Trigger
==========

#About  
SOSTrigger is an Android app that demos the MetaWear board and Android API.  By using the API, the app will use the onboard 
button to send a text message that says, "SOS!  Need Help!" to a recipient when pressed.

#Build
##Configuration
The project is compiled in Eclipse with compiler comliance level 1.7.  It is targetted for SDK 19 and requires, at minimuum, 
SDK 18.  You will need to have the files for SDK 19 (Android 4.4.2) as well as an install of Java 7.

##Issues
1. Due to a quirk in Eclipse, importing the project requires you to have the project initially outside the workspace.  In the 
import projects window, check the "Copy projects into workspace" checkbox and the project will be imported to your Eclipse 
workspace.  
2. Make sure the project's compiler compliance level is set to 1.7.  You can check and/or change this under the "Java Compiler" 
section in the project properties page.

#Usage
To use the app, enter the name of the contact you wish to text.  While entering the name, a dropdown list of possible matches 
will appear for convenience.  After selecting your contact, you can search for your MetaWear with the "Connect" menu item.  A 
popup window of nearby devices will appear during the search.  When you have selected your board, you can press the button to 
send SOS texts to your contact.  

#Questions or Comments
If you have any questions or comments about the project, leave us a post on our [community page](http://community.mbientlab.com/)
