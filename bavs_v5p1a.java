import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;  

import java.util.* ;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
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
/* ----------------------------------------------------------------*/

class time_server extends Thread
{ NetworkSimulator network ;
  TreeMap sms_map=new TreeMap() ;
  VsComm server_comm ;
  TimeMessage request_message=new TimeMessage() ;
  TimeMessage answer_message=new TimeMessage() ;
  OutputFrame out ;

  InetAddress answer_address ;
  int answer_port ;

  String String_to_length(String s , int l)
    { while(s.length()<l) s=s+" " ; return(s) ; }

   time_server(NetworkSimulator n , int  server_port )
     {
     network=n ;
     sms_map.put(new Integer(123),"SMS123") ;
     sms_map.put(new Integer(server_port),"is my port") ;
     sms_map.put(new Integer(4711),"SMS von 4711") ;
     out=new OutputFrame("SERVER at port "+server_port) ;
     server_comm=new VsComm(server_port, network) ;
     out.listln("Server Socket Start..");
     }

  public void run()
  { System.out.println("VS_server.run executed");
    out.listln("Server Start..");

    /* main server loop */
    while(true) {
 //      out.list("wait: ");
       server_comm.receive(0) ; // wait for a request
//       out.list("GOT:");
       request_message.buffer.contents=server_comm.inDatagram.getData() ;
       request_message.unpackFromBuffer() ;
//       out.listln("REQUEST:\n"+request_message);

       /* prepare answer message already */
       request_message.id+=100000 ;
       answer_message=request_message ;
       answer_address=server_comm.inDatagram.getAddress() ;
       answer_port=server_comm.inDatagram.getPort() ;

       /* do request specific actions */
       if (request_message.command==Constants.request_store)
         { out.listln("ENTER("+request_message.number+","+request_message.sms+")");
         sms_map.put(new Integer(request_message.number),request_message.sms) ;
         answer_message.command=Constants.answer_done ;
         }
       else if (request_message.command==Constants.request_delete)
         { out.listln("DELETE("+request_message.number+")");
         sms_map.remove(new Integer(request_message.number)) ;
         answer_message.command=Constants.answer_done ;
         }
      else if (request_message.command==Constants.request_list)
         { out.listln("LIST IS:") ;
           Collection c=sms_map.keySet() ;  Iterator i=c.iterator() ;
           while(i.hasNext())
             {
             Object o=i.next() ;
             out.listln( String_to_length(o.toString(),8)+" : "
                 +String_to_length(sms_map.get(o).toString(),20)+" //") ;
             }
           out.listln("END OF LIST --------") ;
          answer_message.command=Constants.answer_done ;
         }
       else if (request_message.command==Constants.request_first)
         { Collection c=sms_map.keySet() ;  Iterator i=c.iterator() ;
           if(i.hasNext())
             {
             Object o=i.next() ;
             out.listln("First is:"+String_to_length(o.toString(),8)+" : "
                 +String_to_length(sms_map.get(o).toString(),20)+" //") ;
             answer_message.number=((Integer)o).intValue() ;
             answer_message.sms=(String) sms_map.get(o) ;
             answer_message.command=Constants.answer_done ;
             }
            else
             {
             answer_message.sms="<<EMPTY>>" ;
             answer_message.command=Constants.answer_notfound ;
             }
         }
      else if (request_message.command==Constants.request_find)
         { out.listln("FIND("+request_message.number+")");
         String s=new String("") ;
         if (sms_map.containsKey(new Integer(request_message.number)))
           { answer_message.command=Constants.answer_found ;
             answer_message.sms=(String) sms_map.get(new Integer(request_message.number)) ;
           }
          else
           { answer_message.command=Constants.answer_notfound ;
             answer_message.sms="<<UNKNOWN>>" ;
           } ;
         }

      /* and send answer back */
      answer_message.packToBuffer();
      server_comm.OutBuffer=answer_message.buffer.contents ;
      server_comm.message_id=answer_message.id;
      server_comm.send(answer_port,answer_address);
      }
   }
}




/* ----------------------------------------------------------------*/

class SmsClient { 
  int local_time ;
  InetAddress serverAddress ;
  int server_port ;
  int my_port ;
  TimeMessage TX_message=new TimeMessage() ;
  TimeMessage RX_message=new TimeMessage() ;
  VsComm client_socket ;
  OutputFrame out ;

  SmsClient(
             InetAddress adr ,
             int serverport ,
             int myport,
             NetworkSimulator network,
             OutputFrame out__ )  {
    out=out__ ;
    serverAddress=adr ;
    server_port=serverport ;
    my_port=myport ;
    System.out.println("VS_client setup, server="+adr);
    client_socket=new VsComm(my_port,network) ;
    local_time=0 ;
    }

  public void delete_sms_request(int this_number) {
    TX_message.number=this_number ; TX_message.sms="" ;
    TX_message.command=Constants.request_delete ;
    do_request() ;
    }

  public void find_sms_request(int this_number) {
    TX_message.number=this_number ; TX_message.sms="" ;
    TX_message.command=Constants.request_find ;
    do_request() ;
    }

  public void first_sms_request() {
    TX_message.number=0 ; TX_message.sms="" ;
    TX_message.command=Constants.request_first ;
    do_request() ;
    }

  public void enter_sms_request(int this_number , String this_sms) {
    TX_message.number=this_number ;  TX_message.sms=this_sms ;
    TX_message.command=Constants.request_store ;
    do_request() ; 
    }

  public void list_request() {
    TX_message.number=0 ; TX_message.sms="" ;
    TX_message.command=Constants.request_list ;
    do_request() ;
    }

  public void do_request() { 
    //standard_request() ;
	  reliable_request();
    // spater ersetzen durch:
    // reliable_request() ; 
    }

  public void standard_request() {
    local_time++ ;
    TX_message.id=local_time ;
    TX_message.packToBuffer();
    client_socket.OutBuffer=TX_message.buffer.contents ;
    client_socket.message_id=TX_message.id ;
    client_socket.send(server_port,serverAddress) ;
    client_socket.receive(5000) ;
    RX_message.buffer.contents=client_socket.inDatagram.getData() ;
    RX_message.unpackFromBuffer() ;
    System.out.println("TX id="+TX_message.id+" RX_id="+RX_message.id) ;
    }
  
  public void reliable_request() {
	boolean retry = true;
	do {
		local_time++ ;
		TX_message.id=local_time ;
		TX_message.packToBuffer();
		client_socket.OutBuffer=TX_message.buffer.contents ;
		client_socket.message_id=TX_message.id ;
		client_socket.send(server_port,serverAddress) ;
		client_socket.receive(5000) ;
		RX_message.buffer.contents=client_socket.inDatagram.getData() ;
		RX_message.unpackFromBuffer() ;
		if(TX_message.id == RX_message.id-100000) retry = false;
		else System.out.println("ERROR: SendID doesn't match RequestID");
	}
	while(retry);
	System.out.println("TX id="+TX_message.id+" RX_id="+RX_message.id) ;		
  }

 
  }



/* ----------------------------------------------------------------*/
/* ----------------------------------------------------------------*/

class ClientWindow extends Frame {

  TextArea the_text=new TextArea() ;
  Font listFont=new Font("Monospaced",Font.PLAIN,12) ;
  String last_line ;
  SmsClient client ;
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


ClientWindow(  String title ,
               NetworkSimulator network ,
               InetAddress server_address ,
               int server_port ,
               int my_own_port ) {
  super(title) ;

  /* GUI related things ... */
  setSize(500,170) ;
  Panel pan_a=new Panel() ;
  Button enter_but=new Button("ENTER") ;
  Button delete_but=new Button("DELETE") ;
  Button find_but=new Button("FIND") ;
  Button first_but=new Button("FIRST") ;
  Button list_but=new Button("LIST") ;
  pan_a.add(enter_but)  ;
  pan_a.add(delete_but)  ;
  pan_a.add(find_but)  ;
  pan_a.add(first_but)  ;
  pan_a.add(list_but)  ;

  /* connect buttons with actions */
  enter_but.addActionListener(new enter_listener());
  delete_but.addActionListener(new delete_listener());
  find_but.addActionListener(new find_listener());
  first_but.addActionListener(new first_listener());
  list_but.addActionListener(new list_listener());

  /* global layout */
  Label ln=new Label("SMS:") ;
  Label lm=new Label("NUMBER:") ;
  Panel pan_b=new Panel() ;
  pan_b.add(lm) ;  pan_b.add(matrField) ;
  pan_b.add(ln) ;  pan_b.add(nameField) ;
  setLayout(new BorderLayout()) ;
  add("Center",the_text) ;
  add("South",pan_a) ;
  add("North",pan_b) ;
  // deprecation    show();
    setVisible(true) ;
  /* supply a listing window to the client_object */
  OutputFrame out=new OutputFrame("history of "+title) ;

  /* and install client */
  client=new SmsClient(server_address,server_port,my_own_port,network,out) ;
  }

  class enter_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)
       { listln("ENTER") ;
         String name=nameField.getText() ;
         listln("SMS="+name) ;
         String s=matrField.getText() ;
         int matrikel =Integer.valueOf(s).intValue() ;
         client.enter_sms_request(matrikel,name);
       }
    }

  class delete_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)
      {  listln("DELETE") ;
         String s=matrField.getText() ;
         int number =Integer.valueOf(s).intValue() ;
         client.delete_sms_request(number);
      }
    }

  class find_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)
     {  listln("FIND") ;
         String s=matrField.getText() ;
         int matrikel =Integer.valueOf(s).intValue() ;
         client.find_sms_request(matrikel);
         nameField.setText(client.RX_message.sms);
      }
    }

   class first_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)
     {  listln("FIRST") ;
         client.first_sms_request() ;
         matrField.setText(""+client.RX_message.number);
         if (client.RX_message.command==Constants.answer_notfound)
           { nameField.setText("-EMPTY-") ; }
          else
           { nameField.setText(client.RX_message.sms); }
      }
    }

  class list_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)
        { client.list_request(); }
    }

    }   // end class client_window


/* ----------------------------------------------------------------*/
/* ----------------------------------------------------------------*/

class ClientServerTest {
  InetAddress regetry_server_address;
  InetAddress server_a_address ;
  InetAddress server_b_address ;
  NetworkSimulator global_network ;
  int regetry_server_port;
  int server_a_port ;
  int server_b_port ;


void start()
  {
   global_network=new NetworkSimulator() ;
   global_network.p_loss=0.2 ;
   global_network.p_send=0.3 ;

   global_network.start();

   server_a_port=2345 ;
   time_server server_a=new time_server(global_network,server_a_port) ;
   server_a.start() ;

   server_b_port=2346 ;
   time_server server_b=new time_server(global_network,server_b_port) ;
   server_b.start() ;

   try{ server_a_address = InetAddress.getByName("127.0.0.1");   }
   catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
   ClientWindow window_a=new ClientWindow("CLIENT-A",global_network,server_a_address,server_a_port,4555) ;

   try{ server_b_address = InetAddress.getByName("127.0.0.1");   }
   catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
   ClientWindow window_b=new ClientWindow("CLIENT-B",global_network,server_b_address,server_b_port,4556) ;
  

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
