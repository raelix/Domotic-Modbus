package newRaspiServer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import net.wimpi.modbus.procimg.SimpleProcessImage;

//Se maggiori di 85000 faccio backup xk è passato un mese e vado alla tabella successiva , 
public class DBConnection extends Thread {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private String database;
	private String tablename;
	private static int delayLog = 30;//ESEGUITO OGNI 30 SECONDI 20160 a settimana 80640 al mese 
	LinkedList<String> columnName;
	HashMap<String,Integer> columnMap;
	int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
	int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
	private SimpleProcessImage spi;
	public boolean interrupt = false;


	public DBConnection(SimpleProcessImage spi){
		super();

		this.spi = spi;
		columnName = new LinkedList<String>();
		currentMonth = Calendar.getInstance().get(Calendar.MONTH);
		currentYear =  Calendar.getInstance().get(Calendar.YEAR);
		database = "data_"+currentMonth+"_"+currentYear+".db";
		columnMap = new HashMap<String, Integer>();
	}

	public void run(){
		super.run();
		Thread.currentThread().setName("DBConnection");
		Debug.print("Thread: started "+Thread.currentThread().getName());
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
			firstConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Timer timer = new Timer();
		DataLogger logger = new DataLogger();
		logger.init(spi,columnName,columnMap);
		timer.schedule(logger, 10000, delayLog*1000);
		while(!interrupt){
		}
		if(interrupt){
			logger.cancel();
		}

	}
	
	public class DataLogger extends TimerTask{
		protected HashMap<String,Integer> columnMap;
		protected LinkedList<String> columnName;
		protected SimpleProcessImage spi;
		public void init(SimpleProcessImage spi ,LinkedList<String> columnName,HashMap<String,Integer> columnMap){
			this.spi = spi;
			this.columnName = columnName;
			this.columnMap = columnMap;
			Thread.currentThread().setName("DataLoggerTimer");
			Debug.print("Thread: started "+Thread.currentThread().getName());
		}
		@Override
		public void run() {
			Thread.currentThread().setName("DataLoggerTimer");
			database = "data_"+currentMonth+"_"+currentYear+".db";
			LinkedList<Float> processed = new LinkedList<>();
			if(spi != null){
				for(int i = 0; i < columnName.size(); i++){
					int registerNumber =columnMap.get(columnName.get(i));
					if(columnName.get(i).contains("_energy")){
						calulateEnergy(columnName.get(i),spi.getRegister(columnMap.get(columnName.get(i).split("_energy")[0])).getValue(),processed);
//						Debug.info("Energy Calculated: "+processed.getLast());
					}
					else 
						processed.add((float) spi.getRegister(registerNumber).getValue());//registerNumber 				
				}
				try {
					if(connect.isClosed())connect();
					writeValues(processed);
					connect.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void calulateEnergy(String name,int power,LinkedList<Float> processed){

		try {
			if(connect.isClosed())connect();
			float energy =  (float) getEnergy(name, power);
			//		System.out.println("energy: "+energy);
			processed.add(energy);
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}

	}

	public double getEnergy(String name, int power) throws SQLException, ParseException{
		String getLastEnergyComputed= "SELECT "+name.split("_energy")[0]+","+name+", datetime(date,'localtime') as date  FROM "+tablename+" ORDER BY id DESC LIMIT 0,1";
		preparedStatement = connect.prepareStatement(getLastEnergyComputed);   
		ResultSet rs = preparedStatement.executeQuery();
		float name_energy = 0;
		float powerDb  = power;
		String data = null;
		while ( rs.next() ) {
			name_energy =rs.getFloat(name);
			data = rs.getString("date");
			powerDb = rs.getFloat(name.split("_energy")[0]);
		}
		if(data == null || powerDb == 0 || powerDb == 65535.0)return 0.00000;
		//	System.out.println("Energy computed: "+name_energy+" Last Power: "+powerDb);
		Double kWh = new Double(name_energy);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dataOld = formatter.parse(data);
		NumberFormat nf = NumberFormat.getInstance();
		DecimalFormat four = new DecimalFormat("#0.000000");
		Date dataNew = new Date();
		long diff = dataOld.getTime() - dataNew.getTime();
		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000);
		int diffInDays = (int) diff / (1000 * 60 * 60 * 24);
		diffInDays = - diffInDays;
		diffHours = - diffHours;
		diffMinutes = - diffMinutes;
		diffSeconds = - diffSeconds;
		double toHour = 0.00000;
		toHour += (float)(diffInDays)*24;
		toHour += (float)(diffHours);
		toHour += (float) (diffMinutes) /60;
		toHour += (float) (diffSeconds) /3600;
		//        System.out.println("last log: "+toHour);
		float watts = powerDb;
		String resultEnergy = four.format((watts*toHour)/1000);
		//        System.out.println("produced in interval kWh: "+resultEnergy+" old production:"+kWh);
		kWh += nf.parse(resultEnergy).doubleValue();
		return kWh;
	}

	public boolean connect (){
		try {
			connect = DriverManager.getConnection("jdbc:sqlite:"+Configuratore.database+database);
			return connect.isClosed() ? false: true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void writeValues(LinkedList<Float> processed) throws SQLException{

		String appendQuery = "INSERT INTO "+tablename+" (";
		for(int i = 0 ; i < columnName.size(); i++){
			appendQuery+= columnName.get(i)+", ";			  
		}

		appendQuery+="date ) values (";
		for(int i = 0 ; i < processed.size(); i++){
			if(processed.get(i) == -1 || processed.get(i) >= 65535.0 || processed.get(i) >= 65535)
				appendQuery+= "'"+0+"',";
			else 
				appendQuery+= "'"+processed.get(i)+"',";
		}
		appendQuery+= "datetime('now'));";
		preparedStatement = connect.prepareStatement(appendQuery);
		preparedStatement.executeUpdate();
	}

	public void getField(){
		Configuratore conf = new Configuratore();
		conf.readFromFile();
		columnName = new LinkedList<>();
		columnMap = new HashMap<String, Integer>();
		tablename = "dataHistory_"+currentMonth+"_"+currentYear;
		if(conf.GpioEnable)
			for (int i = 0 ; i < conf.pinsUsed.size(); i++){
				if(conf.pinsUsed.get(i).getLog() == 1){
					columnName.add(""+conf.pinsUsed.get(i).getName());
					columnMap.put( conf.pinsUsed.get(i).getName(),i);
				}
			}
		if(conf.DevicesEnable){
			LinkedList<Slave> list = new LinkedList<Slave>();

			list = conf.slaves;
			for (int i = 0 ; i < list.size(); i++){
				for(int f = 0 ; f < list.get(i).getRegisters().size() ; f++){
					if(list.get(i).getRegisters().get(f).getLog() == 1){
						columnName.add(""+list.get(i).getRegisters().get(f).getName());
						columnMap.put( list.get(i).getRegisters().get(f).getName(),(int)(calculatePosition(i, f, conf)));
						if(list.get(i).getRegisters().get(f).getScope().contains("gauge")){
							columnName.add(list.get(i).getRegisters().get(f).getName()+"_energy");
							columnMap.put( list.get(i).getRegisters().get(f).getName()+"_energy",-1);
						}
					}
				}
			}
		}
		//		System.out.println("Table name: "+tablename);
		Debug.print("Starting log database devices: "+Arrays.toString(columnName.toArray()));
	}


	public void firstConnection() throws Exception{
		System.setProperty( "java.library.path", "libsqlitejdbc.so" );//CASE RASPIAN
		getField();
		String creating = "CREATE TABLE IF NOT EXISTS "+tablename+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,date DATETIME);";
		connect = DriverManager.getConnection("jdbc:sqlite:"+Configuratore.database+database); 
		preparedStatement = connect.prepareStatement(creating);
		preparedStatement.executeUpdate();
		for(int i = 0; i < columnName.size(); i++){
			insertColumn(columnName.get(i));
		}
		connect.close();

	}
	public boolean insertColumn(String columnName) throws SQLException{

		try{
			preparedStatement = connect.prepareStatement("alter table "+tablename+" add column "+columnName+" REAL");
			preparedStatement.executeUpdate();
			return true;
		}
		catch(SQLException e){
			if(e.getMessage().contains("duplicate")){
				Debug.info("Column Name duplicate...Skipping "+columnName);
				return false;
			}
			return false;
		}
	}

	public int calculatePosition(int slave , int registerNumber,Configuratore conf){
		if(conf == null)
			conf = new Configuratore();
		int position = 1;
		if(conf.GpioEnable)
			position+=conf.pinsUsed.size();
		for(int i = 0; i < slave; ++i){
			position +=conf.slaves.get(i).getRegisters().size();
		}
		position+=registerNumber;
		return position;
	}

	public Connection getConnect() {
		return connect;
	}

	public void setConnect(Connection connect) {
		this.connect = connect;
	}
	public Statement getStatement() {
		return statement;
	}
	public void setStatement(Statement statement) {
		this.statement = statement;
	}
	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}
	public void setPreparedStatement(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}
	public ResultSet getResultSet() {
		return resultSet;
	}
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getTablename() {
		return tablename;
	}
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

}