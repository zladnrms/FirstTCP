package zladnrms.defytech.firsttcp.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import zladnrms.defytech.firsttcp.Packet.ChatPacket;
import zladnrms.defytech.firsttcp.Packet.EntryPacket;
import zladnrms.defytech.firsttcp.Packet.HeaderPacket;

/*
 * 다중 채팅 구현 파일. Linux 위에 올려놓는다. 그리고 javac ChatServer.java를 통해 class 파일로 컴파일하고, java ChatServer로 실행하여 놓는다.
 */
public class TCPServer {

    HashMap clientMap; // 채팅 목록
    ServerSocket serverSocket = null;
    Socket socket = null;

    // 날짜 계산
    Calendar c;
    SimpleDateFormat sdf;

    public static void main(String[] args) {
        TCPServer ms = new TCPServer(); //서버객체 생성.
        ms.init();//실행.
    }

    public TCPServer(){
        c = Calendar.getInstance();
        sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        clientMap = new HashMap(); //클라이언트의 출력스트림을 저장할 해쉬맵 생성.
        Collections.synchronizedMap(clientMap); //해쉬맵 동기화 설정.
    }

    public void init(){
        try{
            serverSocket = new ServerSocket(9999); //9999포트로 서버소켓 객체생성.
            System.out.println("# 채팅 서버 OPEN #");
            System.out.println("# 현재 시각 : " + sdf.format(c.getTime()) +" #");

            while(true){ //서버가 실행되는 동안 클라이언트들의 접속을 기다림.
                socket = serverSocket.accept(); //클라이언트의 접속을 기다리다가 접속이 되면 Socket객체를 생성.
                System.out.println("다음 IP에서 접속하였습니다 : " + socket.getInetAddress()+":"+socket.getPort()); //클라이언트 정보 (ip, 포트) 출력

                Thread msr = new MultiServerRec(socket); //쓰레드 생성.
                msr.start(); //쓰레드 시동.
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // 클라이언트로부터 읽어온 메시지를 다른 클라이언트(socket)에 보내는 역할을 하는 메서드
    class MultiServerRec extends Thread {

        // 사용 Socket, Stream
        Socket socket;
        OutputStream os;
        InputStream is;
        ObjectOutputStream oos;
        ObjectInputStream ois;

        // 패킷에 담을 정보
        String usernick, chat;
        int roomId;
        int dataLength;

        // 패킷 클래스
        EntryPacket entryPacket;
        ChatPacket chatPacket;

        public MultiServerRec(Socket socket){
            this.socket = socket;
            try{
                os = socket.getOutputStream();
                is =  socket.getInputStream();;
                oos = new ObjectOutputStream(os);
                ois = new ObjectInputStream(is);
            }catch(Exception e){
                System.out.println("예외:"+e);
            }
        }

        @Override
        public void run(){ //쓰레드를 사용하기 위해서 run()메서드 재정의

            try {

                System.out.println("User 한 명이 접속을 시도합니다.");


                while (ois != null) {

                    HeaderPacket headerPacket = (HeaderPacket) ois.readObject();
                    byte requestCode = headerPacket.getRequestCode();
                    System.out.println("받고나서 코드"+requestCode);

                    switch (requestCode) {
                        case 0: // 입장 패킷


                            entryPacket = (EntryPacket) headerPacket;
                            usernick = entryPacket.getUserNick();
                            roomId = entryPacket.getRoomId();

                            clientMap.put(usernick, oos); //해쉬맵에 키를 name으로 출력스트림 객체를 저장.

                            sendAllEnterMsg(roomId, usernick);
                            System.out.println(usernick + "님이 입장히셨습니다");

                            System.out.println("현재 접속자 수는 " + clientMap.size() + "명 입니다.");
                            break;

                        case 1: // 퇴장 패킷
                            entryPacket = (EntryPacket) headerPacket;
                            usernick = entryPacket.getUserNick();
                            roomId = entryPacket.getRoomId();

                            clientMap.remove(usernick);

                            sendAllExitMsg(roomId, usernick);
                            System.out.println(usernick + "님이 나가셨습니다");
                            socket.close();
                            break;

                        case 2: // CHAT 패킷
                            chatPacket = (ChatPacket) headerPacket;
                            usernick = chatPacket.getUserNick();
                            roomId = chatPacket.getRoomId();
                            chat = chatPacket.getChat();
                            sendAllChatMsg(roomId, usernick, chat);
                            break;
                    }
                }//while()---------
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }

        //접속된 모든 클라이언트들에게 입장 메시지를 전달.
        public void sendAllEnterMsg(int roomId, String usernick){

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while(it.hasNext()){
                try{
                    setPacketInfo(usernick, roomId);
                    EntryPacket entryPacket = new EntryPacket(roomId, usernick, dataLength, 0);
                    ObjectOutputStream oos = (ObjectOutputStream) clientMap.get(it.next());
                    oos.writeObject(entryPacket);
                    oos.flush();
                }catch(Exception e){
                    System.out.println("예외:"+e);
                }
            }
        }

        //접속된 모든 클라이언트들에게 퇴장 메시지를 전달.
        public void sendAllExitMsg(int roomId, String usernick){

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while(it.hasNext()){
                try{
                    setPacketInfo(usernick, roomId);
                    EntryPacket entryPacket = new EntryPacket(roomId, usernick, dataLength, 1);
                    ObjectOutputStream oos = (ObjectOutputStream) clientMap.get(it.next());
                    oos.writeObject(entryPacket);
                    oos.flush();
                }catch(Exception e){
                    System.out.println("예외:"+e);
                }
            }
        }

        //접속된 모든 클라이언트들에게 채팅 메시지를 전달.
        public void sendAllChatMsg(int roomId, String usernick, String chat){

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while(it.hasNext()){
                try{
                    setPacketInfo(usernick, chat);
                    ChatPacket chatPacket = new ChatPacket(roomId, usernick, chat, dataLength);
                    ObjectOutputStream oos = (ObjectOutputStream) clientMap.get(it.next());
                    oos.writeObject(chatPacket);
                    oos.flush();

                    System.out.println(usernick + "=>" + chat);
                }catch(Exception e){
                    System.out.println("예외:"+e);
                }
            }
        }

        // 채팅 시 Packet 설정
        private void setPacketInfo(String usernick, String chat) {
            String packet_Data = roomId + "|" + usernick + "|" + chat; // "룸id|유저닉네임|대화내용" 으로 패킷 설정
            dataLength = packet_Data.length();
        }

        // 입장, 퇴장 시 Packet 설정
        private void setPacketInfo(String usernick, int roomId) {
            String packet_Data = roomId + "|" + usernick; // "룸id|유저닉네임" 으로 패킷 설정
            dataLength = packet_Data.length();
        }
    }
}