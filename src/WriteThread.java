import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class WriteThread implements Runnable {

    private MulticastSocket multicastSocket;
    private InetAddress group;
    private int port;
    static String nickname = "";
    static boolean nicknameAvaible = false;
    static boolean resendNicknameBusy = false;
    static boolean nicknameSetup = false;

    public WriteThread(MulticastSocket multicastSocket, InetAddress group, int port) {
        this.multicastSocket = multicastSocket;
        this.group = group;
        this.port = port;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        //setup nickname
        System.out.println("Podaj swoj nickname:");
        nickname = scanner.nextLine();


        while(Chat.exit){
            try {

                String msg;
                msg = scanner.nextLine();
                msg = "[" + nickname + "]: " + msg;

                byte[] buffer = msg.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, group, port);
                multicastSocket.send(datagramPacket);

                if(msg.contains("EXIT")){
                    break;
                }

            } catch (SocketTimeoutException e){
                System.out.println("Your nickname is avaible. You have joined the chat.");
                nicknameSetup = true;
            } catch (IOException e) {
                System.out.println("Program closed.");
            }
        }
    }
}
