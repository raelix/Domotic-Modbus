package newRaspiServer;

public class Debug {

	public Debug(){
	}

	public static void print(String output){
		if(Configuratore.debug == -1);
		else if(Configuratore.debug == 0)System.out.println(output);
		else if(Configuratore.debug == 1)System.out.println(output);
	}

	public static void err(String output){
		System.err.println(output);	
	}
	public static void info(String output){
		if(Configuratore.debug == -1);
		else if(Configuratore.debug == 0);
		else if(Configuratore.debug == 1)
		System.out.println(output);
	}
	public static void infoMax(String output){
		if(Configuratore.debug == -1);
		else if(Configuratore.debug == 0);
		else if(Configuratore.debug == 2)
		System.out.println(output);
	}


}