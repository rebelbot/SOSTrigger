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
1. Enter a contact name and select contact from the list of matches.  Contacts can be removed by holding the names in the 
selected contacts list
2. Configure what messages you want to send in the "Settings" menu
3. Connect a MetaWear board to the app
4. Send a text message to your contacts by either pressing the onboard button or shaking the board

The app will send texts even if it is in the background.  To stop texts from being sent, either disconnect the MetaWear board or 
kill the app.

#Questions or Comments
If you have any questions or comments about the project, leave us a post on our [community page](http://community.mbientlab.com/)
