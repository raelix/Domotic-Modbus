/*
 * GarbageCollectorCaller.java
 *
 * Created on 29 settembre 2011, 10.50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package newRaspiServer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * Gestisce l'invocazione forzata del Garbage Collector.
 */
public class GarbageCollector {

    public GarbageCollector() {
    }

    public static void callGC() {
    	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	Calendar cal = Calendar.getInstance(); 
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - runtime.freeMemory();
        runtime.gc();
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
	      int noThreads = currentGroup.activeCount();
	      Thread[] lstThreads = new Thread[noThreads];
	      currentGroup.enumerate(lstThreads);
	      for (int i = 0; i < noThreads; i++)
	      Debug.infoMax("Thread No:" + i + " = "+ lstThreads[i].getName());
        Debug.err(dateFormat.format(cal.getTime())+": GarbageCollector. Total Java Memory: " + totalMemory + ". Used: " + usedMemory + ".");
    }
}
