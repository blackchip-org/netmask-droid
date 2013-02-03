package org.blackchip.netmask;

import android.text.InputType;
import android.text.method.NumberKeyListener;

public class IPAddressKeyListener extends NumberKeyListener {

	private static final IPAddressKeyListener instance = 
		new IPAddressKeyListener(); 
	
	private static final char[] VALID_INPUT = new char[] { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.'
	};
	
	private IPAddressKeyListener() { 
	}
	
	public int getInputType() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
	}

	@Override
	protected char[] getAcceptedChars() {
		return VALID_INPUT; 
	}

	public static IPAddressKeyListener getInstance() { 
		return instance; 
	}
	
}