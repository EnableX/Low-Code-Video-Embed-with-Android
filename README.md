# Low-Code-Video-Embed-with-Android

EnableX Video Embed is an easy-to-use Video Calling API loaded with powerful conferencing, collaborative, and reporting features. You can build one-to-one or multiparty video meetings for any application or browser within minutes using its simple yet powerful REST API.

You do not need client-side SDKs to develop an engaging UI layout for a video meeting application. Just select our pre-built UI or you can also design it with the EnableX App Visual Builder, and you can go live within minutes with your fully-functional video meeting application.


 # Documentation
Visit https://www.enablex.io/developer/video/low-code-video-embed/ to view the full Low-Code-Video-Embed developer guide documentation and get started.


The enableX help to the developer community to understand, How the enablex Low-Code-Video-Embed product can be implemented using webview in Android Native App( Java/kotlin).

# How it works

# 1.Add  these  permission 
## Requires camera and mic permission for Audio and video streaming 


 ![GitHub Logo](/images/permission.png)




# 2.You need to use Webview class .Create custom WebChromeClient class for Chrome client  and override onPermissionRequest method:

![GitHub Logo](/images/customchromeclient.png)



# 3.Create a WebUtils helper class to configure the WebView. Here is a possible configuration:

![GitHub Logo](/images/webUtils.png)


# 4.Request the permissions if needed.
 ## Add the ?skipMediaPermissionPrompt parameter to the room URL and load it.

   "low code embed url"?skipMediaPermissionPrompt
 # Like
[TEXT TO SHOW]("https://www.google.co.in/"+"?skipMediaPermissionPrompt")
 
  

