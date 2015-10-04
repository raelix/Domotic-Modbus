package com.domotica.raspimanager.fragments;

import com.domotica.raspimanager.Configuratore;
import com.domotica.raspimanager.MainActivity;
import com.domotica.raspimanager.R;
import com.domotica.raspimanager.SSHConnection;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentConfigurationRaspi extends Fragment implements OnClickListener {
	public static final String ARG_SECTION_NUMBER = "section_number";
	Button start;
	Button stop;
	Button help;
	Button delete;
	Button reboot;
	Configuratore conf = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView		   = inflater.inflate(R.layout.fragment_configuration_raspi,container, false);
		//		TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
		//		dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

		start = (Button) rootView.findViewById(R.id.start);
		stop  = (Button)  rootView.findViewById(R.id.stop);
		help  = (Button)  rootView.findViewById(R.id.help);
		delete =  (Button)  rootView.findViewById(R.id.delete);
		reboot =  (Button)  rootView.findViewById(R.id.reboot);
		start.setOnClickListener(this);
		stop.setOnClickListener(this);
		help.setOnClickListener(this);
		delete.setOnClickListener(this);
		reboot.setOnClickListener(this);
		getConf();

		//		if(GpioFragment.requestTimer!=null)
		//			GpioFragment.requestTimer.cancel();
		//		if(PlotFragment.requestTimer!=null)
		//			PlotFragment.requestTimer.cancel();
		//		if(DevicesFragment.requestTimer!=null)
		//			DevicesFragment.requestTimer.cancel();
		return rootView;
	}


	public Configuratore getConf(){
		if (conf != null) return conf;
		else {
			String lastPath=getActivity().getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
			 conf = new Configuratore(lastPath);
			conf.readFromFile();
			return conf;
		}
	}


	@Override
	public void onClick(View v) {
		if(v == start){
			SSHConnection  connection = new SSHConnection();
			connection.setActivity(FragmentConfigurationRaspi.this.getActivity());
			connection.execute(MainActivity.ip,MainActivity.usr,MainActivity.psw,"a","start");
		}else if(v == stop){
			SSHConnection  connection = new SSHConnection();
			connection.setActivity(FragmentConfigurationRaspi.this.getActivity());
			connection.execute(MainActivity.ip,MainActivity.usr,MainActivity.psw,"b","stop");
		}else if(v == help){
			SSHConnection  connection = new SSHConnection();
			connection.setActivity(FragmentConfigurationRaspi.this.getActivity());
			connection.execute(MainActivity.ip,MainActivity.usr,MainActivity.psw,"c","help");
		}
		else if(v == reboot){
			SSHConnection  connection = new SSHConnection();
			connection.setActivity(FragmentConfigurationRaspi.this.getActivity());
			connection.execute(MainActivity.ip,MainActivity.usr,MainActivity.psw,"c","reboot");
		}
		else if(v == delete){
			askDelete();
		}

	}

	public void askDelete(){
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {	new AlertDialog.Builder(getActivity())
			.setTitle("Scarica Server")
			.setMessage("Se cancelli la configurazione dovrai riavviare l'app?")
			.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new Thread(){
						@Override
						public void run(){
							getConf().delete();
							//							System.out.println(getActivity().getBaseContext().getPackageName());

							Intent i =getActivity().getPackageManager().getLaunchIntentForPackage("com.domotica.raspimanager");

							//							Intent i = getActivity().getBaseContext().getPackageManager()
							//						             .getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
							//						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(i);
							getActivity().finish();
						}
					}.start();
					return;
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { 

				}
			})
			.show();
			}
		});
	}
}
