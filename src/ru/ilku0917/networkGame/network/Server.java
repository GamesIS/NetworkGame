package ru.ilku0917.networkGame.network;

import ru.ilku0917.networkGame.NetworkGame;
import ru.ilku0917.networkGame.model.ConditionClient;
import ru.ilku0917.networkGame.model.ConditionGame;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

public class Server extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] sendData = new byte[1024*50];
    private byte[] receivedData = new byte[256];

    public long lastTimeRequest;

    private static final int AVERAGE_DELAY = 15;  // milliseconds

    private NetworkGame game;

    public Server(NetworkGame game) {
        try {
            socket = new DatagramSocket(4445);
        }
        catch (SocketException e){
            e.printStackTrace();
        }
        this.game = game;
    }

    public void run() {
        running = true;
        Random random = new Random();

        while (running) {
            DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
            DatagramPacket sendPacket;
            System.out.println("test");
            try {
                socket.receive(receivePacket);
                InetAddress address = receivePacket.getAddress();
                int port = receivePacket.getPort();

                try {
                    receivedData = receivePacket.getData();
                    lastTimeRequest = System.currentTimeMillis();
                    if(!game.isConnectionSuccessfuly()){
                        game.connectionRestored();
                        NetworkGame.gameStart = true;
                    }
                    ByteArrayInputStream in = new ByteArrayInputStream(receivePacket.getData());
                    ObjectInputStream is = new ObjectInputStream(in);
                    Object receivedObject = is.readObject();
                    is.close();
                    if (receivedObject instanceof ConditionClient){
                        game.getRemotePlayer().setUP(((ConditionClient) receivedObject).UP);
                        game.getRemotePlayer().setDOWN(((ConditionClient) receivedObject).DOWN);
                        game.getRemotePlayer().setMousePressed(((ConditionClient) receivedObject).mousePressed);
                        game.getRemotePlayer().setMousePosition_X(((ConditionClient) receivedObject).mousePosition_X);
                        game.getRemotePlayer().setMousePosition_Y(((ConditionClient) receivedObject).mousePosition_Y);
                        game.setPing(((ConditionClient) receivedObject).ping);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(new ConditionGame(game));

                        sendData = outputStream.toByteArray();
                        sendPacket = new DatagramPacket(sendData, sendData.length, address, port);

                        //Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));

                        socket.send(sendPacket);
                        os.close();
                    }
                }
                catch (ClassNotFoundException e){
                    e.printStackTrace();
                }
                /*catch (InterruptedException e){
                    e.printStackTrace();
                }*/
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        socket.close();
    }
}