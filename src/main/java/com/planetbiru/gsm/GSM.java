package com.planetbiru.gsm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.google.common.base.Splitter;
import com.google.common.primitives.Longs;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GSM {
	private static final Logger logger = LogManager.getLogger(GSM.class);

    private SerialPort serialPort;
    private String portName = "";

	private boolean closed = true;    
    
    private static String[] smsStorage = new String[]{
    		"MT", 
    		"SM"
    	};
    
    public GSM()
    {
    	logger.info("Constructor GSM called");
    }  
   
    /**
     * Execute AT command
     *
     * @param at : the AT command
     * @param waitingTime
     * @return String contains the response
     * @throws GSMException 
     */
    public String executeAT(String at, int waitingTime) throws GSMException 
    {
    	System.out.println("AT Command : "+at);
    	if(getSerialPort() == null)
    	{
    		throw new GSMException("GSM is not initilized yet");
    	}
        at = at + "\r\n";
        String result = "";
        int i = 0;
        byte[] bytes = at.getBytes();
        int written = this.serialPort.writeBytes(bytes, bytes.length);
        if(written > 0)
        {
	        while ((result.trim().equals("") || result.trim().equals("\n")) && i < waitingTime) 
	        {
	            try 
	            {
	                i++;
	                Thread.sleep(500);
	            } 
	            catch (InterruptedException e) 
	            {
	                logger.error(e.getMessage());
	                Thread.currentThread().interrupt();
	            }
	        }
        }
        return result;
    }

    /**
     * Execute USSD command
     *
     * @param ussd : the USSD command
     * @return String contains the response
     * @throws GSMException 
     */
    public String executeUSSD(String ussd) throws GSMException 
    {
    	// executeAT("AT+CUSD=1", 1);
        String cmd = "AT+CUSD=1,\"" + ussd + "\",15";
        String result = "";
        // serialPort.writeBytes((cmd).getBytes(), cmd.getBytes().length);
        executeAT(cmd, 2);
        if(result.contains("ERROR")) 
        {
            logger.info("USSD error");
            return result;
        }
        String str = "";
        result = "";
        int waiting = 0;
        while ((result.trim().equals("") || result.trim().equals("\n")) && waiting < 10) 
        {
            try 
            {
                waiting++;
                Thread.sleep(1000);
            } 
            catch (InterruptedException e) 
            {
                logger.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        if(result.contains("+CUSD")) 
        {
            str = result.substring(12, result.length() - 6);
            logger.info(str);
            /*
            for huawei e173 just return the pure result, no need for extra treatment
            String[] arr = str.split("(?<=\\G....)");
            Iterable<String> arr = Splitter.fixedLength(4).split(str);
            str = "";
            for (String s : arr) {
                int hexVal = Integer.parseInt(s, 16);
                str += (char) hexVal;
            }
            */
        }
        return str;
    }
    

    /**
     * Read the SMS stored in the sim card
     *
     * @return ArrayList contains the SMS
     * @throws GSMException 
     */
    public List<SMS> readSMS() throws GSMException 
    {
        executeAT("ATE0", 1);
        executeAT("AT+CSCS=\"GSM\"", 1);
        executeAT("AT+CMGF=1", 1);
        ArrayList<SMS> str = new ArrayList<>();
        for (String value : smsStorage) 
        {
        	String result = "";
        	String result1 = executeAT("AT+CPMS=\"" + value + "\"", 1);
			if(result1.isEmpty())
        	{
        		result = result1;
        	}

        	String result2 = executeAT("AT+CMGL=\"ALL\"", 5);
			if(result2.isEmpty())
        	{
        		result = result2;
        	}
        	
            if (result.contains("+CMGL")) 
            {
                String[] strs = result.replace("\"", "").split("(?:,)|(?:\r\n)");
                SMS sms;
                for (int i = 1; i < strs.length - 1; i++) 
                {
                    sms = new SMS();
                    sms.setId(Integer.parseInt(strs[i].charAt(strs[i].length() - 1) + ""));
                    sms.setStorage(value);
                    i++;
                    sms.setStatus(strs[i]);
                    i++;
                    sms.setPhoneNumber(strs[i]);
                    i++;
                    sms.setPhoneName(strs[i]);
                    i++;
                    sms.setDate(strs[i]);
                    i++;
                    sms.setTime(strs[i]);
                    i++;
                    if (Longs.tryParse(strs[i].substring(0, 2)) != null) 
                    { 
                    	//get the message UNICODE
                        Iterable<String> arr = Splitter.fixedLength(4).split(strs[i]);
                        StringBuilder con = new StringBuilder();
                        for (String s : arr) 
                        {
                            int hexVal = Integer.parseInt(s, 16);
                            con.append((char) hexVal);
                        }
                        sms.setContent(con.toString());
                    } 
                    else 
                    {
                    	//get the message String
                        sms.setContent(strs[i]);
                    }
                    if (!strs[i + 1].equals("") && !strs[i + 1].startsWith("+")) 
                    {
                        i++;
                        sms.setContent(sms.getContent() + "\n" + strs[i]);
                        i++;
                    }
                    str.add(sms);
                    if (strs[i + 1].equals("") && strs[i + 2].equals("OK")) 
                    {
                        break;
                    }
                }
            }
        }
        return str;
    }

    /**
     * Send an SMS
     *
     * @param recipient the destination number
     * @param message the body of the SMS
     * @return ?
     * @throws GSMException 
     */
    public String sendSMS(String recipient, String message) throws GSMException 
    {
    	String result = "";
    	recipient = recipient.trim();
    	message = message.trim();
    	System.out.println("Send SMS to "+recipient+" "+message+" port "+this.getSerialPort().toString()+" "+this.portName);
    	String result1 = executeAT("ATE0", 1);
    	if(result1.isEmpty())
    	{
    		result = result1;
    	}
    	String result2 = executeAT("AT+CSCS=\"GSM\"", 1);
    	if(result2.isEmpty())
    	{
    		result = result2;
    	}
    	String result3 = executeAT("AT+CMGF=1", 1);
    	if(result3.isEmpty())
    	{
    		result = result3;
    	}
    	String result4 = executeAT("AT+CMGS=\"" + recipient + "\"", 2);
    	if(result4.isEmpty())
    	{
    		result = result4;
    	}
    	String result5 = executeAT(message, 2);
    	if(result5.isEmpty())
    	{
    		result = result5;
    	}
    	String result6 = executeAT(Character.toString((char) 26), 10);
    	if(result6.isEmpty())
    	{
    		result = result6;
    	} 	
        return result;
    }

    public String deleteSMS(int smsId, String storage) throws GSMException 
    {
    	String result = "";
    	String result1 = executeAT("AT+CPMS=\"" + storage + "\"", 1);
    	if(result1.isEmpty())
    	{
    		result = result1;
    	}
    	String result2 = executeAT("AT+CMGD=" + smsId, 1);
    	if(result2.isEmpty())
    	{
    		result = result2;
    	}
        return result;
    }

    public String deleteAllSMS(String storage) throws GSMException 
    {
    	String result = "";
    	result = executeAT("AT+CPMS=\"" + storage + "\"", 1);
    	result = executeAT("AT+CMGD=0, 4", 1);
        return result;
    }

    /**
     * Initialize the connection
     *
     * @param portName the port name
     * @return true if port was opened successfully
     */
    public boolean connect(String portName) throws SerialPortInvalidPortException
    {
    	
    	this.portName = portName;
    	logger.info("INIT port : {}", portName);
    	System.out.println(portName);
   		setSerialPort(SerialPort.getCommPort(portName));
    	if(this.serialPort.openPort()) 
        {
    		
    		this.serialPort.addDataListener(new SerialPortDataListener() 
            {
                @Override
                public int getListeningEvents() 
                {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) 
                {
                    byte[] msg = new byte[getSerialPort().bytesAvailable()];
                    getSerialPort().readBytes(msg, msg.length);
                    String result = new String(msg);
                    onChangeStateSerial(result);
                    
                    
                    //event.getEventType()
                    
                    /*
                     * switch (event.getEventType()) {
					  case SerialPortEvent.BI:
					  case SerialPortEvent.OE:
					  case SerialPortEvent.FE:
					  case SerialPortEvent.PE:
					  case SerialPortEvent.CD:
					  case SerialPortEvent.CTS:
					  case SerialPortEvent.DSR:
					  case SerialPortEvent.RI:
					  case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
					    break;
					  case SerialPortEvent.DATA_AVAILABLE:
					    byte[] readBuffer = new byte[20];
					
					    try {
					      while (inputStream.available() > 0) {
					        int numBytes = inputStream.read(readBuffer);
					      }   
					      System.out.print(new String(readBuffer));
					    } catch (IOException e) {
					    }   
					    break;
					  }   
                     */
                }
            });
            // Prepare for USSD
//            executeAT("AT^USSDMODE=0", 1);
//            if (result.equals(""))
//                return false;
            // turn off periodic status messages (RSSI status, etc.)
//            executeAT("AT^CURC=0", 1);
    		this.closed = false;
            return true;
        } 
        else 
        {
        	System.out.println("FAILED....");
            return false;
        }
    }
    
    public void onChangeStateSerial(String message)
    {
    	logger.info("Receive Message {}", message);
    }

    /**
     * Return list of the available port
     *
     * @return list contains list of the available port
     */
    public String[] getSystemPorts() 
    {
        String[] systemPorts = new String[SerialPort.getCommPorts().length];
        for (int i = 0; i < systemPorts.length; i++) 
        {
            systemPorts[i] = SerialPort.getCommPorts()[i].getSystemPortName();
        }
        return systemPorts;
    }

    /**
     * Close the connection
     *
     * @return true if port was closed successfully
     */
    public boolean closePort() 
    {
        if(getSerialPort() != null)
        {
        	boolean cls = getSerialPort().closePort();
        	this.closed = cls;
        	return cls;
        }
        else
        {
        	return true;
        }
    }

	public SerialPort getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(SerialPort serialPort) {
		this.serialPort = serialPort;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	
}