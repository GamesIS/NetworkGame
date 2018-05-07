package ru.ilku0917.networkGame.network;

import ru.ilku0917.networkGame.NetworkGame;
import ru.ilku0917.networkGame.model.ConditionClient;
import ru.ilku0917.networkGame.model.ConditionGame;

import java.io.*;
import java.net.*;
import java.util.Date;

public class Client {
    private DatagramSocket socket;
    private InetAddress address;
    private NetworkGame game;
    private int ping = 0;

    private static final int MAX_TIMEOUT = 1000;

    private byte[] sendData = new byte[256];
    private byte[] receiveData = new byte[1024 * 50];

    public Client(NetworkGame game) {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(MAX_TIMEOUT);
            address = InetAddress.getByName(NetworkGame.getAddress());//InetAddress.getByName("localhost");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.game = game;
    }

    public void updateCondition() {
        DatagramPacket sendPacket;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        long msSend;
        long msReceive;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(new ConditionClient(game.getCurrentPlayer(), ping));
            os.close();
            msSend = new Date().getTime();
            sendData = outputStream.toByteArray();
            if(!game.isConnectionSuccessfuly()){
                game.connectionRestored();
            }
            sendPacket = new DatagramPacket(sendData, sendData.length, address, 4445);
            socket.send(sendPacket);

            socket.receive(receivePacket);
            msReceive = new Date().getTime();
            ping = (int) (msReceive - msSend);
            game.setPing(ping);

            sendData = receivePacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(sendData);
            ObjectInputStream is = new ObjectInputStream(in);
            try {
                ConditionGame properties = (ConditionGame) is.readObject();
                game.setProperties(properties);
                is.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        catch (SocketException e){
            if(game.isConnectionSuccessfuly()){
                game.lostConnection();
            }
        }
        catch (SocketTimeoutException e) {
            System.out.println("Timeout reached!!! " + e);
            //TODO Здесь должно быть аварийное закрытие соединения
            if(game.isConnectionSuccessfuly()){
                game.lostConnection();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){

    }

    public void close() {
        socket.close();
    }
}