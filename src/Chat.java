import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class Chat {

    public static Boolean exit = true;
    static MulticastSocket multicastSocket;

    public static void main(String[] args) {

        InetAddress group;
        int port;

        try {

            group = InetAddress.getByName("224.0.1.0");
            port = 8008;

            multicastSocket = new MulticastSocket(port);
            multicastSocket.setTimeToLive(0);
            multicastSocket.joinGroup(group);

            Thread writeThread = new Thread(new WriteThread(multicastSocket,group,port));
            writeThread.start();

            while(exit){

                byte[] buffer = new byte[1000];
                DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length,group,port);
                String msg;

                multicastSocket.receive(datagramPacket);
                msg = new String(buffer,0,datagramPacket.getLength(),"UTF-8");

                if (msg.contains("EXIT")){
                    exit = false;
                    multicastSocket.leaveGroup(group);
                    multicastSocket.close();
                    break;
                }

                if(msg.startsWith("[")){
                    System.out.println(msg);
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void checkNicknames(String msg){
        if(msg.toUpperCase().contains(WriteThread.nickname)){
            WriteThread.resendNicknameBusy = true;
        }
    }

}
