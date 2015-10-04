package com.google.android.gcm.server;

import java.io.IOException;

public class prova {
public static void main(String[] argv) throws IOException{
	Sender sender = new Sender("AIzaSyDEZDpgDCEIcTX7O3WxOFEtpgi2p5iUDrg");//API KEY FORNITA DA GOOGLE Key for browser applications
	Message message = new Message.Builder()
	    .addData("message", "thisistheamessage")
	    .addData("other-parameter", "some value")
	    .build();
	Result result = sender.send(message, "APA91bFEkQmPZfDc_3xnvzWD5iAIY8vfQO2mbFuHJytU-33zvMZjk-2imvjZ6CBDN4EWfOKRFVp5f3FMs-j3K_Isr548hPBtEnI_IQ_cW9OkzVAi9lPNPPKnacCdyI-GqEqW13Yf-LJ2qCb58LynI_aLRDoeQTUmwuDzq0GrymhCPNKVDxy9s7M", 3);
}
}
//Chiave di Google:
//AIzaSyDEZDpgDCEIcTX7O3WxOFEtpgi2p5iUDrg
//Chiave del Dispositivo:
//APA91bFEkQmPZfDc_3xnvzWD5iAIY8vfQO2mbFuHJytU-33zvMZjk-2imvjZ6CBDN4EWfOKRFVp5f3FMs-j3K_Isr548hPBtEnI_IQ_cW9OkzVAi9lPNPPKnacCdyI-GqEqW13Yf-LJ2qCb58LynI_aLRDoeQTUmwuDzq0GrymhCPNKVDxy9s7M