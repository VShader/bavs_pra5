import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;  

import java.util.* ;
import java.util.Map.Entry;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;


class OutputFrame extends Frame{
  /*
  Simple frame for
    output texts to a window,
    switch logging on and off,
    save window contents to file
  */

  TextArea the_text=new TextArea() ;
  Font listFont=new Font("Monospaced",Font.PLAIN,12) ;
  boolean is_on=true ;
  String last_line="" ;
  BufferedWriter input_writer ;
  String list_name ;
  String list_path="" ;

  String add_crlf(String s)
    { String s_crlf="" ;
      char c ; int k,len ;
      len=s.length() ;
      for ( k=0 ; k<len ; k++ )
        { c=s.charAt(k) ;
          if ( c=='\n' ) { s_crlf=s_crlf+(char)13+(char)10 ; }
              else { s_crlf=s_crlf+c ; }
        }
    return s_crlf ;
    }

  OutputFrame(String title)
   { super(title) ;
   list_name=title ;
   setSize(300,170) ;
   the_text.setFont(listFont);
   Panel pan_a=new Panel() ;
   Button on_but   =new Button("ON") ;
   Button off_but  =new Button("OFF") ;
   Button list_but =new Button("SAVE") ;
   Button clear_but=new Button("CLEAR") ;
   pan_a.add(on_but   )  ;
   pan_a.add(off_but  )  ;
   pan_a.add(list_but )  ;
   pan_a.add(clear_but)  ;
   on_but.addActionListener(new on_listener());
   off_but.addActionListener(new off_listener());
   list_but.addActionListener(new list_listener());
   clear_but.addActionListener(new clear_listener());
   setLayout(new BorderLayout()) ;
   add("Center",the_text) ;
   add("South",pan_a) ;
  // deprecation    show();
    setVisible(true) ;

 //   addWindowListener(new lister_wlistener()) ;
    }

  void checkit()
    { if ( the_text.getCaretPosition()>10000 )
      { the_text.replaceRange("-- TEXT DELETED --\n",0,5000); }
    }

  void listln(String s)
    { if ( is_on)
        { checkit() ; the_text.append(s); the_text.append("\n") ; last_line="" ;} }

  void list(String s) {
     if ( is_on) {
       checkit() ;
       the_text.append(s);
       last_line=last_line+s ;
       if ( last_line.length()>128)
          { the_text.append("\\LF\n") ; last_line="" ; }
      } }

  void start() { is_on=true ; }

  void stop() {  is_on=false ; }

  void save_edit_area(String full_name)
    { try { input_writer=new BufferedWriter(new FileWriter(full_name)) ; }
    catch(Exception e) { listln("ERR WRopen["+full_name+"] e="+e) ; return ; }

    String s=the_text.getText() ;
    s=add_crlf(s) ;
    try { input_writer.write(s); }
    catch(Exception e) { listln("ERR write["+full_name+"] e="+e) ; return ; }

    try {input_writer.close() ; }
    catch(Exception e) {listln("ERR WRclose"+e) ; return ; }
    } // end save_edit_area

  /*  Button handlers :  */
  class clear_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)  { the_text.setText(""); ;
    last_line="" ; }
    } // end class clear_listener

  class on_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)  { start() ; }
    } // end class on_listener

  class off_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)  { stop() ; }
    } // end class on_listener

  class list_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)
       {   String full_name=list_name+".LST" ;  save_edit_area(full_name) ;  }
    } // end class on_listener

// class lister_wlistener extends WindowAdapter
//   {
//   public void windowClosing(WindowEvent e)   { }
//   public void windowIconified(WindowEvent e)   {  }
//   public void windowDeiconified(WindowEvent e)  {  }
//   }

   } // end class output_frame


/* ----------------------------------------------------------------*/


class ByteBuffer
{ byte[] contents =new byte[512] ;   // buffer for various things as bytes
  int ptr ;                          // index of first unused byte in buffer

  void reset() { ptr=0 ; }

  void putByte(byte b)  { contents[ptr++]=b ;  }
  void putByte(int i)  { contents[ptr++]=(byte) i ;  }

  byte getByte()  { return(contents[ptr++]) ; }

  int getUnsignedByte() { 
    int i=contents[ptr++] ;
    if (i<0) i=i+256 ;  
    return(i) ;  
    }

  void putInt(int i) { 
    putByte( (byte)(i>>0) ) ;  
    putByte( (byte)(i>>8) ) ;
    putByte( (byte)(i>>16) ) ;
    putByte( (byte)(i>>24) ) ;
    }

 int getInt() { 
    int v=getUnsignedByte()      ;
    v=v+(getUnsignedByte() <<8)  ;
    v=v+(getUnsignedByte() <<16) ;
    v=v+(getUnsignedByte() <<24) ;
    return(v) ;
  }

  void putLong(long i) { 
    putByte( (byte)(i>>0) ) ;  
    putByte( (byte)(i>>8) ) ;
    putByte( (byte)(i>>16) ) ;
    putByte( (byte)(i>>24) ) ;
    putByte( (byte)(i>>32) ) ;  
    putByte( (byte)(i>>40) ) ;
    putByte( (byte)(i>>48) ) ;
    putByte( (byte)(i>>56) ) ;
    }
 

  long getLong() { 
    long v=getUnsignedByte()      ;   
    v=v+( ( (long)getUnsignedByte() ) <<8)  ;
    v=v+( ( (long)getUnsignedByte() ) <<16)  ;
    v=v+( ( (long)getUnsignedByte() ) <<24)  ;
    v=v+( ( (long)getUnsignedByte() ) <<32)  ;
    v=v+( ( (long)getUnsignedByte() ) <<40)  ;
    v=v+( ( (long)getUnsignedByte() ) <<48)  ;
    v=v+( ( (long)getUnsignedByte() ) <<56)  ;
 
    return(v) ;
  }


  void putString(String S) { 
    int k ; int v ;
    putByte(S.length()) ;
    for (k=0 ; k<S.length() ; k++ ){
      putByte((byte) S.charAt(k)) ;
      }
    }

  String getString() {
    int l=getUnsignedByte() ;
    String s="" ;
    for (int i=0 ; i<l ; i++) {
      s=s+(char)getByte() ; 
      }
    return(s) ;
    }
  
  void putBoolean(boolean B) {
	  putByte( (byte)(B?1:0) );
  }
  
  boolean getBoolean()	{
	  if(getUnsignedByte() == 0) return false;
	  else return true;
  }

} /* end class byte_buffer */

/* ----------------------------------------------------------------*/

class Packet{
  DatagramSocket socket ;
  DatagramPacket Datagramm ;
  int id ;
  int delays ;
  }

/* ----------------------------------------------------------------*/

class NetworkSimulator extends Thread { 
  final int tick_time=100 ; // ms
  int local_time=0 ;
  OutputFrame output=new OutputFrame("NET-SIM") ;;
  Random Random_loss=new Random(123) ;
  Random Random_delay=new Random(456) ;
  double p_loss=0 ;    // standard setting: no loss
  double p_send=1.0 ;  // standard setting: direct delivery

synchronized void delay(){ 
  try { wait(tick_time) ; local_time++ ; }
  catch(Exception e) { System.out.println("Exception in my_timer e="+e);  System.exit(0); } ;
  }

public void run() { 
  output.listln("NET Start..");
  while(true){
    delay() ; 
    consume() ; 
    }
  }

Vector packetList=new Vector() ;

synchronized public void consume(){ 
  Packet pp ;
  int i=0 ;
  synchronized(packetList) {
    while (i< packetList.size()) { 
      if ( Random_delay.nextFloat()<p_send) {
        pp=(Packet) packetList.remove(i) ;
        packetList.trimToSize();
        try{ pp.socket.send(pp.Datagramm); }
        catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
        output.listln("OUT("+pp.id+") after "+pp.delays+" ticks") ;
        }
       else {
        pp=(Packet) packetList.get(i) ;
        pp.delays++ ; 
        i++ ; 
        }
      } // end while
    } // end synchronized
  }

synchronized public void packetSend(Packet p)  {
  // output.list("SEND "+p.id+" ADR="+p.Datagramm.getAddress()+" port "+p.Datagramm.getPort()) ;
  float r=Random_loss.nextFloat() ;
  if ( r > p_loss) {
    if (p_send>=1.0) {
      try{ p.socket.send(p.Datagramm); }
      catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
      output.listln("DIRECT("+p.id+")");
      }
     else {
      p.delays=0 ;
      synchronized( packetList) {  packetList.add(p) ; } ;
      output.listln("IN("+p.id+")");
      }
    }
   else {
    output.listln("LOST("+p.id+")");
    }
  } // end method
} // end class


/* ----------------------------------------------------------------*/

class VsComm
{ DatagramSocket VS_socket ;
  DatagramPacket inDatagram ;
  byte[] OutBuffer ;
  DatagramPacket outDatagram;
  byte[] inBuffer =new byte[512] ;
  Packet out_packet ;
  NetworkSimulator network ;
  int message_id ;

   VsComm(int port_no , NetworkSimulator net)
    {
    network=net ;
    System.out.println("Try socket get "+port_no);
    try{ VS_socket = new DatagramSocket(port_no); }
    catch(Exception ex) { System.out.print("VsComm : caught : "+ex) ; System.exit(0) ; }
    System.out.println("VS_COMM: got a VS socket :") ;
    inDatagram=new DatagramPacket(inBuffer,inBuffer.length) ;
   }

  void send(int destinationPort , InetAddress destinationAddress) { 
    outDatagram =
         new DatagramPacket(OutBuffer,OutBuffer.length, destinationAddress,destinationPort);
    out_packet=new Packet() ;
    out_packet.Datagramm=outDatagram ;
    out_packet.socket=VS_socket ;
    out_packet.id=message_id ;
    network.packetSend(out_packet);
    } /* end send */


  int receive(int timeout) {
    try{
      VS_socket.setSoTimeout(timeout);
      inDatagram=new DatagramPacket(inBuffer,inBuffer.length) ;
      VS_socket.receive(inDatagram);
      } catch(Exception ex) { 
        // System.out.println("RX timeout ") ; 
        return(1) ; }
//    System.out.println("got VS packet from: "+inDatagram.getAddress()+
//    " port "+inDatagram.getPort()+"  Length="+inDatagram.getLength() );
    inBuffer=inDatagram.getData() ;
    return(0) ;
    } // end receive
 } // end class vs_comm


/* ----------------------------------------------------------------*/
/* ----------------------------------------------------------------*/


class TimeMessage{

int id ;				// message id given from request
long time ;   			// server time
String serverName ;

ByteBuffer buffer=new ByteBuffer() ;

  public String toString()
  { return("#"+serverName+" Time:"+time+" id="+id) ; }

  TimeMessage()
  { time=0;  serverName="empty";  id=456; }

  void packToBuffer()
  {
  buffer.reset() ;
  buffer.putInt(id);
  buffer.putString(serverName);
  buffer.putLong(time);
  }

  void unpackFromBuffer()
  {
  buffer.reset() ;
  id=buffer.getInt() ;
  serverName=buffer.getString();
  time=buffer.getLong();
  }
}

class ReportMessage{

int id ;				// message id given from request
String serverName ;
String ip;				// server ip
int port ;   			// server port
boolean valid;

ByteBuffer buffer=new ByteBuffer() ;

  public String toString()
  { return("#"+serverName+" IP:"+" Port:"+port+" id="+id) ; }

  ReportMessage()
  { id=456; serverName="empty"; ip="127.0.0.1"; port=0; valid=true;}

  void packToBuffer()
  {
  buffer.reset() ;
  buffer.putInt(id);
  buffer.putString(serverName);
  buffer.putString(ip);
  buffer.putInt(port);
  buffer.putBoolean(valid);
  }

  void unpackFromBuffer()
  {
  buffer.reset() ;
  id=buffer.getInt() ;
  serverName=buffer.getString();
  ip=buffer.getString();
  port=buffer.getInt();
  valid=buffer.getBoolean();
  }
}
/* ----------------------------------------------------------------*/

class time_server extends Thread
{ NetworkSimulator network ;
  VsComm server_comm ;
  TimeMessage time_request_message=new TimeMessage() ;
  TimeMessage time_answer_message=new TimeMessage() ;
  ReportMessage report_request_message=new ReportMessage() ;
  ReportMessage report_answer_message=new ReportMessage() ;
  OutputFrame out ;
  String serverName;
  InetAddress ip;
  int port;
  InetAddress regestryIp;
  int regestryPort;
  public boolean running;
  int lastID;

  InetAddress answer_address ;
  int answer_port ;

  String String_to_length(String s , int l)
    { while(s.length()<l) s=s+" " ; return(s) ; }

   time_server(String serverName, NetworkSimulator n, InetAddress regestry_server_address, int regestry_server_port, 
		   		InetAddress server_address, int  server_port)
     {
	 running = true;
     network=n ;
     this.serverName=serverName;
     ip = server_address;
     port=server_port;
     regestryIp = regestry_server_address;
     regestryPort = regestry_server_port;
     out=new OutputFrame(serverName+" at port "+server_port) ;
     server_comm=new VsComm(server_port, network) ;
     out.listln("Server Socket Start..");
     }

  public void run()
  { System.out.println("VS_server.run executed");
    out.listln("Server Start..");
    sendIp();

    while(true)	{
//    if(running) Report to the RegestryServer	
    	/* main server loop */
    	while(running) {
    		sendTime();
    	}
    	try { sleep(100); }
    	catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
    	out.listln("Server "+serverName+" stopped!!!");
    }
  }
    
    public void sendTime()	{
//      out.list("wait: ");
        server_comm.receive(5000) ; // wait for a request, after 5s send request to registryserver
//         out.list("GOT:");
        time_request_message.buffer.contents=server_comm.inDatagram.getData() ;
        time_request_message.unpackFromBuffer() ;
        if(time_request_message.id == lastID+100000 || time_request_message.id == lastID)
        {
        	out.listln("#################################################");
        	out.listln("# ERROR: No request, try to connect to regestry #");
        	out.listln("#################################################");
        	sendIp();
        	return;
        }
//         out.listln("REQUEST:\n"+request_message);
        lastID = time_request_message.id;

         /* prepare answer message already */
        time_request_message.id+=100000 ;
        time_answer_message=time_request_message ;
        answer_address=server_comm.inDatagram.getAddress() ;
        answer_port=server_comm.inDatagram.getPort() ;
        
        out.listln("Message from: "+server_comm.inDatagram.getAddress().toString().substring(1)+" "+server_comm.inDatagram.getPort());

         /* send time */
        time_answer_message.serverName = serverName;
        time_answer_message.time = System.currentTimeMillis();
        time_answer_message.packToBuffer();
        server_comm.OutBuffer=time_answer_message.buffer.contents ;
        server_comm.message_id=time_answer_message.id;
        
        out.listln("SEND ("+time_answer_message.serverName +"," +time_answer_message.time/1000 +"s)");
        out.listln("to "+answer_address.toString().substring(1)+" "+answer_port);
        
        server_comm.send(answer_port,answer_address);
        out.listln("SEND ("+time_answer_message.serverName +"," +time_answer_message.time/1000 +"s)");
        out.listln("to "+answer_address.toString().substring(1)+" "+answer_port);
    }
    
    void sendIp()	{
    	boolean retry=true;
    		report_request_message.serverName = serverName;
    		report_request_message.ip = ip.toString().substring(1);
    		report_request_message.port = port;
    		report_request_message.packToBuffer();
    		server_comm.OutBuffer=report_request_message.buffer.contents ;
    		server_comm.message_id=500;
    		server_comm.send(regestryPort,regestryIp);
    		out.listln("SEND ("+time_answer_message.serverName +"," +time_answer_message.time/1000 +"s)");
    }
}


/* ----------------------------------------------------------------*/
/* ----------------------------------------------------------------*/

class TimeWindow extends Frame {

	  TextArea the_text=new TextArea() ;
	  Font listFont=new Font("Monospaced",Font.PLAIN,12) ;
	  String last_line ;
	  time_server server ;
	  TextField nameField=new TextField(20) ;
	  TextField matrField=new TextField(8) ;

	/* my own list window outout functions */

	  void checkit() { 
	    if ( the_text.getCaretPosition()>25000 )
	      { the_text.replaceRange("-- TEXT DELETED --\n",0,5000); }
	    }

	  void listln(String s) {
	    checkit() ; the_text.append(s); the_text.append("\n") ; last_line="" ;}

	  void list(String s) {
	       checkit() ; the_text.append(s);
	       last_line=last_line+s ;
	       if ( last_line.length()>128)
	          { the_text.append("\\LF\n") ; last_line="" ; }
	      }

	/* real stuff starts here ....                                   */

TimeWindow(  String title ,
      NetworkSimulator network ,
      InetAddress regestry_server_address,
      int regestry_server_port,
      InetAddress server_address ,
      int my_own_port ) {
super(title) ;

/* GUI related things ... */
setSize(500,170) ;
Panel pan_a=new Panel() ;
Button start_but=new Button("Start") ;
Button stop_but=new Button("Stop") ;
pan_a.add(start_but)  ;
pan_a.add(stop_but)  ;

/* connect buttons with actions */
start_but.addActionListener(new start_listener());
stop_but.addActionListener(new stop_listener());

/* global layout */
setLayout(new BorderLayout()) ;
add("Center",the_text) ;
add("South",pan_a) ;
//add("North",pan_b) ;
//deprecation    show();
setVisible(true) ;
/* supply a listing window to the client_object */
//OutputFrame out=new OutputFrame("Status "+title) ;

/* and install client */
server=new time_server(title, network, regestry_server_address, regestry_server_port, 
	server_address, my_own_port);
server.start();
}

class start_listener implements ActionListener
{ public void actionPerformed(ActionEvent e)
{ listln("Start") ;
//String name=nameField.getText() ;
server.running = true;
}
}

class stop_listener implements ActionListener
{ public void actionPerformed(ActionEvent e)
{ listln("Stop") ;
//String name=nameField.getText() ;
server.running = false;
}
}


}   // end class time_window


/* ----------------------------------------------------------------*/

class Regestry_server extends Thread
{ NetworkSimulator network ;
  TreeMap server_map=new TreeMap();
  int nextServer;
  VsComm server_comm ;
  ReportMessage request_message=new ReportMessage() ;
  ReportMessage answer_message=new ReportMessage() ;
  OutputFrame out ;
  String serverName;
  boolean running;

  InetAddress answer_address ;
  int answer_port ;

  String String_to_length(String s , int l)
    { while(s.length()<l) s=s+" " ; return(s) ; }

   Regestry_server(String serverName, NetworkSimulator n, InetAddress server_address, int  server_port)
     {
	 running = true;
     network=n ;
     this.serverName=serverName;
     nextServer=0;
     out=new OutputFrame(serverName+" at port "+server_port) ;
     server_comm=new VsComm(server_port, network) ;
     out.listln("Server Socket Start..");
     }

  public void run()
  { System.out.println("VS_server.run executed");
    out.listln("Server Start..");

    /* main server loop */
    while(running) {
    	listServer();
 //      out.list("wait: ");
      server_comm.receive(0) ; // wait for a request
//       out.list("GOT:");
      request_message.buffer.contents=server_comm.inDatagram.getData() ;
      request_message.unpackFromBuffer() ;
//       out.listln("REQUEST:\n"+request_message);
      if(request_message.valid) addServer();
      else deleteServer();

    }
  }
  
  void addServer()	{
	  server_map.put(new String(request_message.serverName), new serverContainer(request_message.serverName,
			  																	request_message.ip, request_message.port));
      out.listln("SEND ("+answer_message.serverName +"," +"s)");

  }
  
  void deleteServer()	{
	  server_map.remove(new String(request_message.serverName));
	  int counter=0;
	  Iterator<Map.Entry<String, serverContainer>> entries = server_map.entrySet().iterator();
	  while(entries.hasNext())
	  {
		  if( counter == nextServer%server_map.size())
		  {
			  nextServer++;
			  Map.Entry<String, serverContainer> entry = entries.next();
			  request_message.id+=100000 ;
		      answer_message=request_message ;
			  answer_message.valid = true;			// only to show the client that this is the answer.
		      answer_address=server_comm.inDatagram.getAddress() ;
		      answer_port=server_comm.inDatagram.getPort() ;

		      answer_message.serverName = entry.getValue().serverName;
		      answer_message.ip = entry.getValue().ip;
		      answer_message.port = entry.getValue().port;
		      answer_message.packToBuffer();
		      server_comm.OutBuffer=answer_message.buffer.contents ;
		      server_comm.message_id=answer_message.id;
		      server_comm.send(answer_port,answer_address);
		      out.listln("SEND ("+answer_message.serverName +"," +"s)");
		      return;
		  }
		  counter++;
	  }
  }
  
  void listServer()	{
	  out.listln("List all Servers:");
	  Iterator<Map.Entry<String, serverContainer>> entries = server_map.entrySet().iterator();
	  while(entries.hasNext())
	  {
		  nextServer++;
		  Map.Entry<String, serverContainer> entry = entries.next();
		  out.listln(entry.getValue().serverName);

	  }
	  out.listln("");

  }
  
  class serverContainer	{
	  public String serverName;
	  public String ip;
	  public int port;
	  public serverContainer(String sN, String i, int p)	{
		  serverName=sN; ip=i; port=p;
	  }
	  
  }
}


/* ----------------------------------------------------------------*/

class TimeClient extends Thread{ 
  int local_time ;
  InetAddress regestryServerAddress ;
  InetAddress timeServerAddress ;
  int regestryServer_port ;
  int timeServer_port ;
  String timeServerName;
  int my_port ;
  long RTime;
  ReportMessage TX_Report_message=new ReportMessage() ;
  ReportMessage RX_Report_message=new ReportMessage() ;
  TimeMessage TX_Time_message=new TimeMessage() ;
  TimeMessage RX_Time_message=new TimeMessage() ;
  VsComm client_socket ;
  OutputFrame out ;

  TimeClient(
		  	 String clientName,
		  	 NetworkSimulator network,
             InetAddress adr ,
             int serverport ,
             int myport )  {
    out=new OutputFrame(clientName) ;
    regestryServerAddress=adr ;
    regestryServer_port=serverport ;
    my_port=myport ;
    System.out.println("VS_client setup, server="+adr);
    client_socket=new VsComm(my_port,network) ;
    local_time=0 ;
    try{ timeServerAddress=InetAddress.getByName("0.0.0.0"); }
    catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
    timeServerName="";
    }
  
  public void run()
  {
	  server_request();
	  time_request();
  }
  
  public void server_request()	{
	  boolean retry = true;
		do {
			local_time++ ;
			TX_Report_message.id=local_time ;
			TX_Report_message.serverName=timeServerName;
			TX_Report_message.ip=timeServerAddress.toString();
			TX_Report_message.port=timeServer_port;
			TX_Report_message.valid=false;
			TX_Report_message.packToBuffer();
			client_socket.OutBuffer=TX_Report_message.buffer.contents ;
			client_socket.message_id=TX_Report_message.id ;
			client_socket.send(regestryServer_port, regestryServerAddress) ;
			client_socket.receive(5000) ;
			RX_Report_message.buffer.contents=client_socket.inDatagram.getData() ;
			RX_Report_message.unpackFromBuffer() ;
			if(TX_Report_message.id == RX_Report_message.id-100000) retry = false;
			else	{
				System.out.println("ERROR: SendID doesn't match RequestID");
				System.out.println("Request faild or no server is available");
			}
		}
		while(retry);
		
		timeServerName=RX_Report_message.serverName;
		try {timeServerAddress=InetAddress.getByName(RX_Report_message.ip);}
		catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
		timeServer_port=RX_Report_message.port;
		out.listln("New Timeserver "+timeServerName+"\nIP:"+timeServerAddress.toString().substring(1)+" Port:"+timeServer_port) ;
  }

  
 public void time_request() {
	  Timer timer=new Timer();
	  while (true) {
		  TX_Time_message.time=0 ;  TX_Time_message.serverName="" ;
		  reliable_request() ;
		  out.listln("Time from "+RX_Time_message.serverName+" is "+RTime/1000+"s");
			  try{ this.sleep(1000); }
			  catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
	  }
    }

  
  public void reliable_request() {
	boolean retry = true;
	int timeout = 3;
	do {
		local_time++ ;
		TX_Time_message.id=local_time ;
		TX_Time_message.packToBuffer();
		client_socket.OutBuffer=TX_Time_message.buffer.contents ;
		client_socket.message_id=TX_Time_message.id ;
		client_socket.send(timeServer_port, timeServerAddress) ;
		client_socket.receive(5000) ;
		RX_Time_message.buffer.contents=client_socket.inDatagram.getData() ;
		RX_Time_message.unpackFromBuffer() ;
		if(TX_Time_message.id == RX_Time_message.id-100000) retry = false;
		else System.out.println("ERROR: SendID doesn't match RequestID");
		timeout--;
		if(timeout == 0)	{
			out.listln("No answer from "+RX_Time_message.serverName+"!!!\nTry to get another.");
			timeout=3;
			server_request();
		}
	}
	while(retry);
	System.out.println("TX id="+TX_Time_message.id+" RX_id="+RX_Time_message.id) ;
	RTime = RX_Time_message.time;
  }

 
  }


/* ----------------------------------------------------------------*/
/* ----------------------------------------------------------------*/



class ClientServerTest {
  InetAddress regestry_server_address;
  InetAddress server_a_address ;
  InetAddress server_b_address ;
  NetworkSimulator global_network ;
  int regestry_server_port;
  int server_a_port ;
  int server_b_port ;


void start()
  {
   global_network=new NetworkSimulator() ;
   global_network.p_loss=0.2 ;
   global_network.p_send=0.3 ;

   global_network.start();

   
   try{ regestry_server_address = InetAddress.getByName("127.0.0.1");   }
   catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
   try{ server_a_address = InetAddress.getByName("127.0.0.1");   }
   catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
   try{ server_b_address = InetAddress.getByName("127.0.0.1");   }
   catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
   
   regestry_server_port=1337;
   Regestry_server regServer=new Regestry_server("Regestry Server", global_network,  regestry_server_address, regestry_server_port);
   regServer.start();
   
   server_a_port=2345 ;
   TimeWindow server_a=new TimeWindow("Server A", global_network, regestry_server_address, 
		   								regestry_server_port, server_a_address, server_a_port) ;
   
   server_b_port=2346 ;
   TimeWindow server_b=new TimeWindow("Server B", global_network, regestry_server_address, 
				regestry_server_port, server_b_address, server_b_port) ;

   
   TimeClient client_a=new TimeClient("Client A", global_network, regestry_server_address,regestry_server_port,4555) ;
   client_a.start();
   
   TimeClient client_b=new TimeClient("Client B", global_network, regestry_server_address,regestry_server_port,4556) ;
   client_b.start();
  }
} // end class client_server_test


/* ----------------------------------------------------------------*/


/* ----------------------------------------------------------------*/


public class bavs_v5p1a {

	public static void main(String[] args) {
	    System.out.println("Hello Verteilte Systeme") ;
	    ClientServerTest test=new ClientServerTest() ;
	    test.start() ;
	    }
}
