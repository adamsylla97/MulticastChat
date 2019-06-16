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
    static boolean waitingForResponse = false;

    public WriteThread(MulticastSocket multicastSocket, InetAddress group, int port) {
        this.multicastSocket = multicastSocket;
        this.group = group;
        this.port = port;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        //setup nickname
        while(!nicknameSetup){
            if(!waitingForResponse){

                //pobranie nicku od uzytkownika
                System.out.println("Podaj swoj nickname:");
                nickname = scanner.nextLine();

                //wyslanie wiadomosci NICK nickname z zapytaniem czy dany nick jest wolny
                byte[] nickBuffer = ("NICK " + nickname).getBytes();
                DatagramPacket nickDatagramPacket = new DatagramPacket(nickBuffer, nickBuffer.length, group, port);
                try {
                    multicastSocket.send(nickDatagramPacket);
                    waitingForResponse = true;
                    multicastSocket.setSoTimeout(10000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
                System.out.println("waiting...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(Chat.exit){
            try {

                String msg;
                msg = scanner.nextLine();
                msg = "[" + nickname + "]: " + msg;

                byte[] buffer = msg.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, group, port);
                multicastSocket.send(datagramPacket);

                if(resendNicknameBusy){
                    sendNicknameBusy();
                }

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

    private void sendNicknameBusy() throws IOException {
        String msg = "NICK " + nickname + " BUSY";
        byte[] buffer = msg.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, group, port);
        multicastSocket.send(datagramPacket);
        resendNicknameBusy = false;
    }
}
