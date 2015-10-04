package com.domotica.raspimanager;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.Timer;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.domotica.raspimanager.shared.BarGraph;

public class ActivityHistoryGraph extends Activity implements OnWheelChangedListener,OnClickListener {
	Timer	timer;
	SQLiteDatabase db;
	boolean secondTIme = false;
	//	RequestTimer requestTimer ;
	private static GraphicalView view;
	private BarGraph pie = new BarGraph();
	private static Thread thread;
	public static String name;
	public static int indirizzo;
	public static int type = 0;//0 energy - 1 power
	public static boolean acceso = true;
	ImageButton chooseDate;
	static Activity act ;
	int graph = 0;//0 line - 1 bar - 2 Pie
	int visual = 0;//0 day - 1 month -2 year
	int years;
	int months;
	int days;
	double total = 0;
	EditText dpResult;
	WheelView GraphType;
	WheelView ViewType;
	Button Energy ;
	Button Power;
	String dbase;
	ProgressDialog pd = null;
	/** Called when the activity is first created. */
	final Calendar c = Calendar.getInstance();

	@Override 
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_historygraph);
		indirizzo = getIntent().getExtras().getInt("indirizzo");
		name = getIntent().getExtras().getString("nome");
		act = this;
		int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
		int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
		dbase = "data_"+currentMonth+"_"+currentYear+".db";
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		if(GpioFragment.requestTimer!=null)
//			GpioFragment.requestTimer.cancel();
//		if(PlotFragment.requestTimer!=null)
//			PlotFragment.requestTimer.cancel();
//		if(DevicesFragment.requestTimer!=null)
//			DevicesFragment.requestTimer.cancel();
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
		years = c.get(Calendar.YEAR);
		months = c.get(Calendar.MONTH);
		days = c.get(Calendar.DAY_OF_MONTH);
		pd = new ProgressDialog(this);
		total = 0;
		GraphType = (WheelView) findViewById(R.id.GraphType);
		ViewType = (WheelView) findViewById(R.id.ViewType);
		chooseDate = (ImageButton) findViewById(R.id.imageButton1);
		dpResult = (EditText) findViewById(R.id.dpResult);
		Energy = (Button) findViewById(R.id.Energy);
		Power = (Button) findViewById(R.id.Power);
		Energy.setOnClickListener(this);
		Power.setOnClickListener(this);
		chooseDate.setOnClickListener(this);
		GraphType.setVisibleItems(1);
		String[] Graph = new String[]{"Barra","Linea","Torta"};
		ArrayWheelAdapter<String> GraphAdapter =
				new ArrayWheelAdapter<String>(this, Graph);
		GraphAdapter.setTextSize(18);
		GraphType.setViewAdapter(GraphAdapter);
		getDb();
		ViewType.setVisibleItems(1);
		String[] View = new String[]{"Giorno","Mese","Anno"};
		ArrayWheelAdapter<String> ViewAdapter =
				new ArrayWheelAdapter<String>(this, View);
		ViewAdapter.setTextSize(18);
		ViewType.setViewAdapter(ViewAdapter);
		GraphType.addChangingListener(this);
		ViewType.addChangingListener(this);
	}


	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			dpResult.setText(selectedDay + " / " + (selectedMonth + 1) + " / "
					+ selectedYear);
			years = selectedYear;
			months = selectedMonth;
			days = selectedDay;
		}
	};


	public void getDb(){
		new Thread(){
			@Override
			public void run(){
				try {
					runProgress();
					if(new File( getApplicationInfo().dataDir+"/*.mark").exists() )new File( getApplicationInfo().dataDir+"/*.mark").delete();
					if(new File( getApplicationInfo().dataDir+"/*data.mark").exists() )new File( getApplicationInfo().dataDir+"/*data.mark").delete();
					SSHConnection	connection = new SSHConnection();
					connection.setActivity(act);

					int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
					int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
					String db = "data_"+currentMonth+"_"+currentYear+".db";
					connection.execute(MainActivity.ip,MainActivity.usr,MainActivity.psw,MainActivity.dir+dbase,"ReadDb",db);

					lockProgress();
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		}.start();
	}

	public void getDbDate(String month,String year){

	}

	class getDateFile extends Thread{
		private String month;
		private String year;

		public getDateFile(String month, String year){
			super();
			this.month = month;
			this.year = year;
		}

		@Override
		public void run(){
			super.run();
			runProgress();

			if(new File( getApplicationInfo().dataDir+"/*.mark").exists() )new File( getApplicationInfo().dataDir+"/*.mark").delete();
			if(new File( getApplicationInfo().dataDir+"/*data.mark").exists() )new File( getApplicationInfo().dataDir+"/*data.mark").delete();
			SSHConnection	connection = new SSHConnection();
			connection.setActivity(act);

			int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
			int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
			String db = "data_"+month+"_"+year+".db";

			connection.execute(MainActivity.ip,MainActivity.usr,MainActivity.psw,MainActivity.dir+db,"ReadDb",db);

			lockProgress();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// set date picker as current date
		return new DatePickerDialog(this, datePickerListener, years, months,days);
	}



	public void runProgress(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				pd.setMessage("Caricamento In Corso..");
				pd.show();
				pd.setCancelable(false);
			}
		});
	}


	public void lockProgress(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(pd != null)
					pd.dismiss();
			}
		});
	}


	public class startGraphEnergy extends AsyncTask<Integer, Void,Integer> {
		int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
		int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
		ProgressDialog pd = null;


		@Override
		protected void onPostExecute(Integer result) {
			lockProgress();

		}

		@Override
		protected void onPreExecute() {
			runProgress();
		}
		@Override//SOLO PER ENERGIA
		protected Integer doInBackground(Integer... params)   {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			int day = days;
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
			int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
			//			if(currentMonth != months || currentYear != years){
			//				//CASO DA CALCOLARE SE VIENE RICHIESTO UN DATO NON CONTENUTO NELL ATTUALE TABELLA, BISOGNA CONTROLLARE SE ESISTE 
			//				getDateFile threadGetter =	new getDateFile(""+months,""+years);
			//				threadGetter.start();
			//			}
			//			while(!new File(MainActivity.dir+"data_"+months+"_"+years).exists());
			String tablename = "dataHistory_"+months+"_"+years;
			openDb();
			pie = new BarGraph();
			String query = "";
			//				int graph = 0;//0 line - 1 bar - 2 Pie
			//				int visual = 0;//0 day - 1 month -2 year
			if(visual == 0){//day query quindi visualizzo le 24 ore 
				query = "SELECT  "+name+"_energy, strftime('%H',  datetime(date,'localtime'),'+58 minutes') as hourcol, datetime(date,'localtime') as date  from "+tablename+" WHERE strftime('%d',  datetime(date,'localtime')) = '"+day+"'  GROUP BY hourcol ORDER BY  datetime(date,'localtime') ASC";
			}
			else if(visual == 1){//mensile quindi i giorni
				query = "SELECT  datetime(date,'localtime') as date, strftime('%d',  datetime(date,'localtime')) as hourcol,"+name+"_energy  from "+tablename+" WHERE strftime('%m',  datetime(date,'localtime')) = '0"+(months+1)+"'  GROUP BY hourcol ORDER BY  datetime(date,'localtime') ASC";
			}
			else if(visual == 2){//annuale ancora non considerato
			}
			System.out.println(query);
			Cursor c = db.rawQuery(query, new String[] {});
			LinkedList<Double> listaDouble = new LinkedList<Double>();
			LinkedList<String> listaString = new LinkedList<String>();
			double first = 0;
			double last = 0;
			try {
				if (c!=null) {
					for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
						double value = c.getDouble(c.getColumnIndex(name+"_energy"));
						listaDouble.add(value);
						String dates = c.getString(c.getColumnIndex("hourcol"));
						listaString.add(dates);
						System.out.println("value "+value+" hourcol "+dates+" date "+(new Date(c.getLong((c.getColumnIndex("date")))).getDate()));
						//						i++;
					}
					if(listaDouble.size()>0 ){
					last =  listaDouble.getLast();
					first = listaDouble.getFirst();
					

					total = last - first;
					}
				}

				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						((TextView) findViewById(R.id.Ene)).setText("Energia: "+Math.floor(total * 1000) / 1000+" kWh");
						((TextView) findViewById(R.id.Spe)).setText("Spesa: "+Math.floor((total*0.23f) * 100) / 100+" €");
					}
				});
				System.out.println("element n "+listaDouble.size());
				for(int j = 0; j < listaDouble.size()-1; j++){
					double result = listaDouble.get(j+1)-listaDouble.get(j);
					if(result < 0 ) result = 0;
					if(visual == 0)
						pie.addElement(""+listaString.get(j)+":00",Math.floor(result * 1000) / 1000);
					else if(visual ==1){
						pie.addElement(listaString.get(j+1),Math.floor(result * 1000) / 1000);
				}
				}

			}
			finally {
				if (c!=null) {
					c.close();
				}
			}

			pie.getData();
			closeDb();
			addpie();
			return 1;
		}
	}

	public class startGraphPower extends AsyncTask<Integer, Void,Integer> {
		int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
		int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
		ProgressDialog pd = null;


		@Override
		protected void onPostExecute(Integer result) {
			lockProgress();

		}

		@Override
		protected void onPreExecute() {
			runProgress();
		}
		@Override//SOLO PER ENERGIA
		protected Integer doInBackground(Integer... params)   {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			int day = days;
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
			int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
			//			if(currentMonth != months || currentYear != years){
			//				getDateFile threadGetter =	new getDateFile(""+months,""+years);
			//				threadGetter.start();
			//			
			//			}
			//			while(!new File(MainActivity.dir+"data_"+months+"_"+years).exists());
			String tablename = "dataHistory_"+months+"_"+years;
			openDb();
			pie = new BarGraph();
			String query = "";
			//				int graph = 0;//0 line - 1 bar - 2 Pie
			//				int visual = 0;//0 day - 1 month -2 year
			if(visual == 0){//day query quindi visualizzo le 24 ore //CORRETTA CONTROLLATA A DB RESTITUISCE la media la colonna ore e la data completa di ore
				//				query = "SELECT  "+name+"_energy, strftime('%H',  datetime(date,'localtime'),'+58 minutes') as hourcol, datetime(date,'localtime') as date  from "+tablename+" WHERE strftime('%d',  datetime(date,'localtime')) = '"+day+"'  GROUP BY hourcol ORDER BY  datetime(date,'localtime') ASC";
				query = "SELECT  AVG("+name+") as "+name+", strftime('%H',  datetime(date,'localtime'),'+58 minutes') as hourcol, datetime(date,'localtime') as date  from "+tablename+" WHERE strftime('%d',  datetime(date,'localtime')) = '"+controlZero(days)+"'  GROUP BY hourcol ORDER BY  datetime(date,'localtime') ASC";
			}
			else if(visual == 1){//mensile quindi i giorni CORRETTA!
				//				query = "SELECT  datetime(date,'localtime') as date, strftime('%d',  datetime(date,'localtime')) as hourcol,"+name+"_energy  from "+tablename+" WHERE strftime('%m',  datetime(date,'localtime')) = '0"+(int)(months+1)+"'  GROUP BY hourcol ORDER BY  datetime(date,'localtime') ASC";
				query = "SELECT  datetime(date,'localtime') as date, strftime('%d',  datetime(date,'localtime')) as hourcol,AVG("+name+") as "+name+"  from "+tablename+" WHERE strftime('%m',  datetime(date,'localtime')) = '"+controlZero(months+1)+"'  GROUP BY hourcol  ORDER BY  datetime(date,'localtime') ASC";
			}
			else if(visual == 2){//annuale ancora non considerato
			}
			System.out.println("quyery i s "+query);
			Cursor c = db.rawQuery(query, new String[] {});
			LinkedList<Double> listaDouble = new LinkedList<Double>();
			LinkedList<String> listaString = new LinkedList<String>();
			total = 0;
			int i = 0;
			try {

				if (c!=null) {
					for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
						double value = c.getDouble(c.getColumnIndex(name));
						String dates = c.getString(c.getColumnIndex("hourcol"));
//						double date = c.getDouble(c.getColumnIndex(("date"));
						System.out.println("value "+value+" hourcol "+dates+" date "+(new Date(c.getLong((c.getColumnIndex("date")))).getDate()));
						total+= value;
						if(visual == 0)
							pie.addElement(""+dates+":00",Math.floor(value * 1000) / 1000);
						else if(visual ==1)
							pie.addElement(dates,Math.floor(value * 1000) / 1000);
						i++;
					}	
					if(i > 0)
					total = total/i;
				}
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						((TextView) findViewById(R.id.Ene)).setText("Potenza Media: "+Math.floor(total * 1000) / 1000+" Watt");
						((TextView) findViewById(R.id.Spe)).setText("");
					}
				});
			}
			finally {
				if (c!=null) {
					c.close();
				}
			}

			pie.getData();
			closeDb();
			addpie();
			return 1;
		}
	}

	@Override
	public void onBackPressed() {
		//	   Log.d("CDA", "onBackPressed Called");
		//	   Intent setIntent = new Intent(Intent.ACTION_MAIN);
		//	   setIntent.addCategory(Intent.CATEGORY_HOME);
		//	   setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//	   startActivity(setIntent);
		//			GpioFragment.timer.scheduleAtFixedRate(GpioFragment.requestTimer,5000, 3000);
		//			PlotFragment.timer.scheduleAtFixedRate(PlotFragment.requestTimer,5000, 3000);
		//			DevicesFragment.timer.scheduleAtFixedRate(DevicesFragment.requestTimer,5000, 3000);
		finish();
	}






	public String controlZero(int value){

		if(value >= 0 && value <= 9) 
			return "0"+value;
		else /*if (value > 9)*/{
			return ""+value;
		}
	}
	public Configuratore getConf(){
		String lastPath=getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
		Configuratore conf = new Configuratore(lastPath);
		conf.readFromFile();
		return conf;
	}

	public void openDb(){
		if(db == null || !db.isOpen())
			db = SQLiteDatabase.openDatabase(MainActivity.dir+dbase,null, 0);
	}

	public void closeDb(){
		if(db.isOpen())db.close();
	}

	public void addpie(){
		act.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//				if(view != null)view.refreshDrawableState();



				ViewGroup layout = ((ViewGroup) findViewById(R.id.history_Activity));
				RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						550);
				//								lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				//								lp.addRule(RelativeLayout.CENTER_IN_PARENT);
				lp.addRule(RelativeLayout.ALIGN_LEFT , R.id.imageViewplots);
				lp.addRule(RelativeLayout.ALIGN_RIGHT , R.id.imageViewplots);
				lp.addRule(RelativeLayout.ALIGN_BOTTOM , R.id.imageViewplots);
				lp.addRule(RelativeLayout.BELOW , R.id.radioGroup1);
				lp.bottomMargin = 150;

				if(!secondTIme){	
					view = pie.getView(act,graph);
					view.setLayoutParams(lp);
					layout.addView(view);

					secondTIme = true;
				}
				else {
					layout.removeView(view);
					view = pie.getView(act,graph);
					view.setLayoutParams(lp);
					layout.addView(view);
				}
				//				view.refreshDrawableState();

			}
		});
	}


	//	String[] Graph = new String[]{"Pie","Line","Bar"};//
	//	String[] View = new String[]{"Giornaliero","Mensile","Annuale"};//

	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if(wheel == ViewType){
			visual = newValue;
		}
		else if(wheel == GraphType){

			if(newValue == 0)graph = 0;
			else if(newValue == 1)graph =1;
			else if(newValue == 2)graph = 2;
			//			addpie();
		}
	}




	@Override
	public void onClick(View v) {
		dbase = "data_"+months+"_"+years+".db";
		if(v == chooseDate ){
			showDialog(0);
		}
		else if (v == Energy){
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
			int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
			if(currentMonth != months || currentYear != years){
				if(!new File(MainActivity.dir+"data_"+months+"_"+years+".db").exists()){
					getDateFile threadGetter =	new getDateFile(""+months,""+years);
					threadGetter.start();
				}
				else new startGraphEnergy().execute();
			}
			else 
				new startGraphEnergy().execute();
		}
		else if(v == Power){
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
			int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
			if(currentMonth != months || currentYear != years){
				if(!new File(MainActivity.dir+"data_"+months+"_"+years+".db").exists()){
					getDateFile threadGetter =	new getDateFile(""+months,""+years);
					threadGetter.start();
				}
				else new startGraphPower().execute();
			}
			else 
				new startGraphPower().execute();
		}
	}

}





//public class RequestTimer extends TimerTask{
//
//	int indirizzo;
//	public RequestTimer(int indirizzo){
//		this.indirizzo = indirizzo;
//	}
//
//	@Override
//	public void run() {
//		System.out.println("Timer!!");
////		new readHR().execute(indirizzo);
//		return;
//
//	}
//}  
//
//public class SetStatus extends AsyncTask<String, Void, String> {
//	ProgressDialog pd = null;
//	@Override
//	protected void onPreExecute() {
//		pd = ProgressDialog.show(HistoryActivity.this, "Please Wait",
//				"Caricamento In Corso..", true);
//		pd.setCancelable(true);
//	}
//
//	@Override
//	protected String doInBackground(String... params) {
//		return null;
//	
//	}
//
//	@Override
//	protected void onPostExecute(String result) {
//		pd.dismiss();
//
//	}
//
//}


//public class readHR extends AsyncTask<Integer, Void,Integer> {
//	int id = 0;
//	ReadMultipleRegistersRequest req = null; //the request
//	ReadMultipleRegistersResponse res = null; //the response
//	int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
//	int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
//	@SuppressLint("NewApi")
//	ProgressDialog pd = null;
//	
//	
//	@Override
//	protected void onPostExecute(Integer result) {
//		pd.dismiss();
//
//	}
//
//	protected void onPreExecute() {
////		 Looper.prepare();
//		pd = ProgressDialog.show(act, "Please Wait",
//				"Caricamento In Corso..", true);
//		pd.setCancelable(true);
//	}
//	@Override
//	protected Integer doInBackground(Integer... params)   {
//		getDb();
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy);
//		int day = (c.get(Calendar.DAY_OF_MONTH));
//		int id = 0;
//		ReadMultipleRegistersRequest req = null; //the request
//		ReadMultipleRegistersResponse res = null; //the response
//		int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
//		int currentYear =  Calendar.getInstance().get(Calendar.YEAR);
//		String tablename = "dataHistory_"+currentMonth+"_"+currentYear;
//		SQLiteDatabase db = SQLiteDatabase.openDatabase(MainActivity.dbPath,null, 0);
//		//		float energyInstant = 0;
//		//		Cursor c = db.rawQuery(attual, new String[] { });
//		//		if (c.moveToFirst()) {
//		//			energyInstant =c.getShort(c.getColumnIndex(name+"_energy"));
//		//		}
//		pie = new BarChartGraph();
//		LinkedList<Double> hourConsumption = new LinkedList<Double>();
//		for(int i = 0; i < 23 ; i++){
//			double todayEnergy = 0 ;
//			int f = i;
//			String first = "";
//			String second = "";
//			if(controlZero((int)(f+1))){
//				first+="0"+(int)(f+1);
//			}
//			else first+=""+(int)(f+1);
//			if(controlZero((int)(f))){
//				second+="0"+(int)(f);
//			}
//			else second+=""+(int)(f);
//			//		db.setLocale(Locale.ITALY);
//			Cursor c = db.rawQuery("SELECT (SELECT "+name+"_energy"+" from "+tablename+" WHERE datetime(date,'localtime') >  strftime('%Y-%m-"+day+" "+first+":%M:%S', 'now', 'localtime') ORDER BY date ASC LIMIT 0,1)"
//					+ " - (SELECT "+name+"_energy"+" from "+tablename+" WHERE datetime(date,'localtime') >  strftime('%Y-%m-"+day+" "+second+":%M:%S', 'now', 'localtime') ORDER BY date ASC LIMIT 0,1) as Hour "
//					+ "from "+tablename+"  "
//					+ "ORDER BY datetime(date,'localtime') DESC LIMIT 0,1", new String[] { });
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if (c.moveToFirst()) {
//				todayEnergy = (double)  c.getDouble(c.getColumnIndex("Hour"));
//				hourConsumption.add(Math.floor(todayEnergy * 1000) / 1000);
//				pie.addElement(""+i, hourConsumption.get(i));
//			}	
//		}
//		pie.getData();
//		db.close();
//		addpie();
//
//		timer.cancel();
//		return null;
//	}
//	
//	
//	
//}
//Scelta tra le ore SELECT * from dataHistory_4_2014 WHERE date >  strftime('%Y-%m-%d 01:%M:%S', 'now') ORDER BY date ASC LIMIT 0,1
//SCELTA TRA LE ORE COMPLETO SELECT (SELECT Vdc_energy from dataHistory_4_2014 WHERE date >  strftime('%Y-%m-%d 01:%M:%S', 'now') ORDER BY date ASC LIMIT 0,1) - (SELECT Vdc_energy from dataHistory_4_2014 WHERE date >  strftime('%Y-%m-%d 00:%M:%S', 'now') ORDER BY date ASC LIMIT 0,1) as Today from dataHistory_4_2014  ORDER BY date DESC LIMIT 0,1
//today energy SELECT Vdc_energy - (SELECT Vdc_energy from dataHistory_4_2014 WHERE date >  date('now','start of day') ORDER BY date ASC LIMIT 0,1) as Today from dataHistory_4_2014  ORDER BY date DESC LIMIT 0,1
//Yesterday   Select (SELECT Vdc_energy from dataHistory_4_2014 WHERE date > date('now','start of day') ORDER BY date ASC LIMIT 0,1) - Vdc_energy  as Vdc_energy from dataHistory_4_2014 WHERE date > date('now','start of day','-1 day') ORDER BY date ASC LIMIT 0,1
//				Connection connect = DriverManager.getConnection("jdbc:sqlite:"+MainActivity.dbPath); 
//				connect = SQLiteDatabase.openDatabase(MainActivity.dbPath, null, SQLiteDatabase.OPEN_READONLY);
//String hour = "SELECT (SELECT Vdc_energy from "+tablename+" WHERE date >  strftime('%Y-%m-%d 01:%M:%S', 'now', 'localtime') ORDER BY date ASC LIMIT 0,1)"
//		+ " - (SELECT Vdc_energy from "+tablename+" WHERE date >  strftime('%Y-%m-%d 00:%M:%S', 'now', 'localtime') ORDER BY date ASC LIMIT 0,1) as Today "
//		+ "from dataHistory_4_2014  "
//		+ "ORDER BY date DESC LIMIT 0,1";
//String attual = "SELECT * from dataHistory_4_2014  ORDER BY date DESC LIMIT 0,1";
//String today = "SELECT * from dataHistory_4_2014 WHERE date >  date('now','start of day') ORDER BY date ASC LIMIT 0,1";
//String yesterday = "SELECT * from dataHistory_4_2014 WHERE date >  date('now','start of day','-1 day') ORDER BY date ASC LIMIT 0,1";
//String thismonth = "SELECT * from dataHistory_4_2014 WHERE date >  date('now','start of month') ORDER BY date ASC LIMIT 0,1 ";
//String lastmonth = "SELECT * from dataHistory_4_2014 WHERE date >  date('now','start of month','-1 month') ORDER BY date ASC LIMIT 0,1";
