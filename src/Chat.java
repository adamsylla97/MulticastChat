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

            while(true){
                byte[] buffer = new byte[1000];
                DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length,group,port);
                String msg;
                try{
                    multicastSocket.receive(datagramPacket);
                    msg = new String(buffer,0,datagramPacket.getLength(),"UTF-8");
                    if(msg.contains("BUSY")){
                        WriteThread.waitingForResponse = false;
                    }
                    if(!msg.startsWith("[")){
                        System.out.println(msg);
                    }
                } catch (SocketTimeoutException e){
                   // System.out.println("Ten nick jest wolny");
                    WriteThread.nicknameSetup = true;
                    multicastSocket.setSoTimeout(0);
                   // System.out.println("Dolaczyles do chatu!");
                    break;
                }
            }

            while(exit){

                byte[] buffer = new byte[1000];
                DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length,group,port);
                String msg;

                multicastSocket.receive(datagramPacket);


                msg = new String(buffer,0,datagramPacket.getLength(),"UTF-8");
                if(msg.contains("NICK")){
                    checkNicknames(msg);
                }

                if (msg.contains("EXIT")){
                    exit = false;
                    multicastSocket.leaveGroup(group);
                    multicastSocket.close();
                    break;
                }

                //if(msg.startsWith("[")){
                    System.out.println(msg);
                //}


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void checkNicknames(String msg){
        //System.out.println("CHECKING NICKNAME");
        //System.out.println(msg.contains(WriteThread.nickname));
        if(msg.contains(WriteThread.nickname)){
            //System.out.println("NICK IS THE SAME");
            WriteThread.resendNicknameBusy = true;
        } else {
            //System.out.println("NICK IS NOT THE SAME");
            WriteThread.resendNicknameBusy = false;
        }
    }

}
