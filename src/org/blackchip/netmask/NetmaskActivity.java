package org.blackchip.netmask;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

public class NetmaskActivity extends Activity {
	
	private final Map<Long,Integer> bits = new HashMap<Long,Integer>(); 
	
	private static final long BITS32 = 0xffffffff;
	private static final NumberFormat formatSeparators = 
		new DecimalFormat("#,###");
	
	private EditText editAddress;
	private EditText editMask; 
	private TextView textNetwork; 
	private TextView textMask; 
	private TextView textBroadcast; 
	private TextView textAddresses; 
	
	public NetmaskActivity() { 
		bits.put(0L, 0);
		long v = 1;
		for ( int i = 1; i <= 32; i++ ) { 
			bits.put(parseMask(String.valueOf(i)), i); 
			v *= 2; 
		}
	}
	
	private long parseAddress(String value) { 
		value = value.trim(); 
		String[] octets = value.split("\\."); 
		if ( octets.length != 4 ) { 
			throw new NumberFormatException(); 
		}
		long address = 0; 
		for ( int i = 0; i < 4; i++ ) {
			address = address << 8; 
			String octet = octets[i];
			int v = Integer.parseInt(octet);
			if ( v < 0 || v > 255 ) { 
				throw new NumberFormatException(); 
			}
			address |= v; 
		}
		return address; 
	}
	
	private boolean isAddressValid(String value) { 
		try { 
			parseAddress(value); 
		} catch ( NumberFormatException nfe ) { 
			return false; 
		}
		return true; 
	}
	
	private String formatAddress(long value) { 
		String address = "";
		for ( int i = 0; i < 4; i++ ) { 
			int v = (int)(value & 0xff);
			String suffix = "";
			if ( address.length() != 0 ) { 
				suffix = ".";
			}
			address = String.valueOf(v) + suffix + address; 
			value = value >> 8; 
		}
		return address; 
	}
	
	private long parseMask(String value) { 
		if ( isAddressValid(value) ) { 
			return parseAddress(value); 
		}
		// CIDR
		value = value.trim(); 
		int v = Integer.parseInt(value);
		if ( v < 0 || v > 32 ) { 
			throw new NumberFormatException(); 
		}
		return ((long)Math.pow(2, v) - 1) << (32-v); 
	}
	
	private boolean isMaskValid(String value) { 
		try { 
			long v = parseMask(value); 
			return bits.containsKey(v); 
		} catch ( NumberFormatException nfe ) { 
			return false; 
		}
	}
			
	private void compute() { 
		String strAddress = editAddress.getText().toString();
		String strMask = editMask.getText().toString(); 
		
		boolean valid = isAddressValid(strAddress) && isMaskValid(strMask); 
		
		if ( valid ) { 
			long address = parseAddress(strAddress);
			long mask = parseMask(strMask); 
			long network = address & mask; 
			long broadcast = network | (~mask); 
			int cidr = bits.get(mask); 
			long addresses = (long)(Math.pow(2, (32 - cidr)) - 2);
			if ( cidr == 32 ) addresses = 1;
			if ( cidr == 31 ) addresses = 2;
			
			textNetwork.setText(formatAddress(network) + "/" + cidr);
			textMask.setText(formatAddress(mask)); 
			if ( cidr == 31 || cidr == 32 ) { 
				textBroadcast.setText(""); 
			} else {
				textBroadcast.setText(formatAddress(broadcast));
			}
			textAddresses.setText(formatSeparators.format(addresses)); 
		} else { 
			textNetwork.setText("");
			textMask.setText(""); 
			textBroadcast.setText(""); 
			textAddresses.setText(""); 
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        editAddress = (EditText)findViewById(R.id.editAddress);
        editAddress.setKeyListener(IPAddressKeyListener.getInstance()); 
        editAddress.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				compute(); 
				return false; 
			} 
        });
        
        editMask = (EditText)findViewById(R.id.editMask);
        editMask.setKeyListener(IPAddressKeyListener.getInstance());
        editMask.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				compute(); 
				return false; 
			} 
        });
                
        textNetwork = (TextView)findViewById(R.id.textNetwork); 
        textMask = (TextView)findViewById(R.id.textMask);
        textBroadcast = (TextView)findViewById(R.id.textBroadcast); 
        textAddresses = (TextView)findViewById(R.id.textAddresses); 
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putString("address", 
    								 editAddress.getText().toString());
    	savedInstanceState.putString("mask", 
    			 					 editMask.getText().toString()); 
     	super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	editAddress.setText(savedInstanceState.getString("address")); 
    	editMask.setText(savedInstanceState.getString("mask")); 
    	compute();
    }
    
}