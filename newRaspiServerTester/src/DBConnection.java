import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

//Se maggiori di 85000 faccio backup xk è passato un mese e vado alla tabella successiva , 
public class DBConnection {
	private static String rowCountQuery = "SELECT Count(*) FROM ";
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private String database;
	private String tablename;
	private int ncampi;
	LinkedList<String> columnName;
	int currentMonth = new Date().getMonth();

	public static void main(String[] args) throws Exception {
		new DBConnection().connection();
	}


	public DBConnection(){
		columnName = new LinkedList<String>();
		currentMonth = new Date().getMonth()+1;
	}



	public void getField(){
		Configuratore conf = new Configuratore();
		conf.readFromFile();
		tablename = "dataHistory_"+currentMonth;
		if(conf.GpioEnable)
			for (int i = 0 ; i < conf.pinsUsed.size(); i++){
				if(conf.pinsUsed.get(i).getLog() == 1){
					columnName.add(""+conf.pinsUsed.get(i).getName());
				}
			}

		if(conf.DevicesEnable){
			LinkedList<Slave> list = new LinkedList<Slave>();
			list = conf.slaves;
			for (int i = 0 ; i < list.size(); i++){
				for(int f = 0 ; f < list.get(i).getRegisters().size() ; f++){
					if(list.get(i).getRegisters().get(f).getLog() == 1)
					columnName.add(""+list.get(i).getRegisters().get(f).getName());
				}
			}
		}
		System.out.println("Table name: "+tablename);
		System.out.println("Starting log device: "+Arrays.toString(columnName.toArray()));
	}


	public void connection() throws Exception{
		getField();
		String creating = "CREATE TABLE IF NOT EXISTS "+tablename+" (ID INTEGER PRIMARY KEY AUTOINCREMENT);";
		String addColumn = "alter table ? add column ? char(50)";
		connect = DriverManager.getConnection("jdbc:sqlite:data.db"); 
		//				preparedStatement.setString(1, "campo");
		preparedStatement = connect.prepareStatement(creating);
		preparedStatement.executeUpdate();
		for(int i = 0; i < columnName.size(); i++){
			insertColumn(columnName.get(i));
		}
		
	}
	

	
	
	
	

	public boolean insertColumn(String columnName) throws SQLException{

		try{
			preparedStatement = connect.prepareStatement("alter table "+tablename+" add column "+columnName+" char(50)");
			int result = preparedStatement.executeUpdate();
			System.out.println(result);
			return true;
		}catch(SQLException e){
			if(e.getMessage().contains("duplicate")){
				System.out.println("Column Name duplicate...Skipping "+columnName);
				return false;
			}
			return false;
		}


	}


	public int calculatePosition(int slave , int registerNumber,Configuratore conf){
		int position = 1;
		conf.readFromFile();
		if(conf.GpioEnable)
			position+=conf.gpioNumber;
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


//public boolean checkexist(String match) throws SQLException{
//	preparedStatement = connect.prepareStatement("pragma table_info('"+tablename+"');");
//	ResultSet resultSet = preparedStatement.executeQuery();
//	if (resultSet.next()) {
//		for(int i = 1 ; i < resultSet.getMetaData().getColumnCount(); i++){
//							System.out.println("column exists: "+resultSet.getString(i));
//		
//			if(resultSet.getString(i) != null && resultSet.getString(i).contains(match)){
//				return true;
//			}
//		}
//	} else {
//		//			System.out.println("column doesn't exists!");
//		return false;
//	}
//	return false;
//}


//mysql_connect('localhost', 'root', '');
//if (!mysql_select_db('mydb')) {
//    echo("creating database!\n");
//    mysql_query('CREATE DATABASE mydb');
//    mysql_select_db('mydb');
//}
//CREATE DATABASE IF NOT EXISTS DBName;