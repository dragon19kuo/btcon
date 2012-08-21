package btcon.btcubs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BtconActivity extends Activity {
    /** Called when the activity is first created. */
	private static final UUID HeadSet_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB"); 
    private static final UUID OBEX_UUID = UUID.fromString("00001105-0000-1000-8000-00805F9B34FB");
    private static final UUID HID_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private final String SD_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String prdName="8XX";
    BluetoothAdapter btAdapt;
    public static BluetoothSocket btSocket;
    BluetoothDevice btDev;
    
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        BtInit();
        Search("JK-SNB");
        chkConnection(btDev);
        SendCmd();
        
    }
    private boolean BtInit()
    {
        btAdapt = BluetoothAdapter.getDefaultAdapter();
        btAdapt.enable();
        return true;
    	
    }
    public void Discovery()
    {
    	if (btAdapt.isDiscovering()) {
    		btAdapt.cancelDiscovery();
        }
    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
    	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);  
        btAdapt.startDiscovery(); 	
    }
    public void Search(String prd)
    {   	
    	 Set<BluetoothDevice> pairedDevices = btAdapt.getBondedDevices();
         if (pairedDevices.size() > 0) {
             for (BluetoothDevice device : pairedDevices) {
                 String name= device.getName();
              	if(name.startsWith(prd))
              	{
              		btDev=device;
              		break;
              	}
                }
           }  
    }
    public void Connect()
    {
   

    }
    public void SendCmd()
    {
    	   try {

               Method m = null;
               try {
                   m = btDev.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
               } catch (SecurityException e) {
                   e.printStackTrace();
               } catch (NoSuchMethodException e) {
                   e.printStackTrace();
               }
               try {
                   btSocket = (BluetoothSocket) m.invoke(btDev, 1);
               } catch (IllegalArgumentException e) {
                   e.printStackTrace();
               } catch (IllegalAccessException e) {
                   e.printStackTrace();
               } catch (InvocationTargetException e) {
                   e.printStackTrace();
               }
               
               String targetAddr=btDev.getAddress();
               String FilePath=SD_PATH+"/1ktone.wav";
               File fp=new File(FilePath);
               boolean f =fp.exists();

               ContentValues cv = new ContentValues();
               cv.put("uri", FilePath);
               cv.put("destination", targetAddr);
               cv.put("direction", 0);
               Long ts = System.currentTimeMillis();
               cv.put("timestamp", ts);
               getContentResolver().insert(Uri.parse("content://com.android.bluetooth.opp/btopp"),cv);
               
               btSocket.close();
           } catch (IOException e) {
               e.printStackTrace();
               //Toast.makeText(context, "?仃韐伐?霂琿??啣?霂?", Toast.LENGTH_LONG).show(); 
           }
    }
	private boolean chkConnection(BluetoothDevice dev)
	{
		BluetoothSocket btsocket=null; 
		try{
			btsocket=dev.createRfcommSocketToServiceRecord(OBEX_UUID);
		}catch (IOException e) {
			return false;
		}	     	
		try{
			btsocket.connect();
			btsocket.close();
		}catch (IOException e) {
			return false;
		}        
		return true;    	 		
	} 
    private void RegEvent()
    {
    	  IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
          this.registerReceiver(mReceiver, filter);
      	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
          this.registerReceiver(mReceiver, filter);  
         	filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
          this.registerReceiver(mReceiver, filter);
          filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
          this.registerReceiver(mReceiver, filter);   
    }
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name= device.getName();
            if	(name.startsWith("8XX")||name.startsWith(prdName))
            {
            	if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            		try{
            			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");   
                        Boolean ret =(Boolean) createBondMethod.invoke(device);    
                        Toast.makeText(getApplicationContext(), name + "added to list", Toast.LENGTH_LONG).show();
            		    
            		}catch (Exception e) {   
                        e.printStackTrace();   
                    }   
            	}                	
            }
        // 當發現藍牙裝置結束時，更改機動程式的標題
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        	Log.d("BlueToothTestActivity", "ACTION_FOUND");   
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name= device.getName();
      	    if(name.startsWith("8XX")||name.startsWith(prdName))
      	    {
                Toast.makeText(getApplicationContext(), name+"Device connected", Toast.LENGTH_LONG).show();
      	    }    
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name= device.getName();
            if(name.startsWith("8XX")||name.startsWith(prdName))
      	    {
            	if(!chkConnection(device))
            	{
                    Toast.makeText(getApplicationContext(), name+"connection lost!!!!", Toast.LENGTH_LONG).show();
                   
            	}
      	    }   
        }
        
    }
    
}; 	
}