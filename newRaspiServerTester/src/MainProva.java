import java.util.LinkedList;


public class MainProva {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
Configuratore c = new Configuratore();
LinkedList<PinRecord> pinner =  new LinkedList<PinRecord>();
pinner.add(new PinRecord("ilNome", "12", "1000", true, true, true, true));
//c.writePin("C:/Users/gianmarco/Desktop/configurationNewRaspiServer.xml",pinner);
	}

}
