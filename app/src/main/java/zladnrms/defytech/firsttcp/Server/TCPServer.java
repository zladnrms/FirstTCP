package zladnrms.defytech.firsttcp.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import zladnrms.defytech.firsttcp.Packet.ChatPacket;
import zladnrms.defytech.firsttcp.Packet.EntryPacket;
import zladnrms.defytech.firsttcp.Packet.GameReadyPacket;
import zladnrms.defytech.firsttcp.Packet.HeaderPacket;
import zladnrms.defytech.firsttcp.Packet.PositionPacket;
import zladnrms.defytech.firsttcp.Packet.StartQueuePacket;

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

    public TCPServer() {
        c = Calendar.getInstance();
        sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        clientMap = new HashMap(); //클라이언트의 출력스트림을 저장할 해쉬맵 생성.
        Collections.synchronizedMap(clientMap); //해쉬맵 동기화 설정.
    }

    public void init() {
        try {
            serverSocket = new ServerSocket(9999); //9999포트로 서버소켓 객체생성.
            System.out.println("# 채팅 서버 OPEN #");
            System.out.println("# 현재 시각 : " + sdf.format(c.getTime()) + " #");

            while (true) { //서버가 실행되는 동안 클라이언트들의 접속을 기다림.
                socket = serverSocket.accept(); //클라이언트의 접속을 기다리다가 접속이 되면 Socket객체를 생성.
                System.out.println("다음 IP에서 접속하였습니다 : " + socket.getInetAddress() + ":" + socket.getPort()); //클라이언트 정보 (ip, 포트) 출력

                Thread msr = new MultiServerRec(socket); //쓰레드 생성.
                msr.start(); //쓰레드 시동.
            }

        } catch (Exception e) {
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
        int position;
        int dataLength;

        // 입장 시 방 유저
        ArrayList<String> userList = new ArrayList<String>();

        // 패킷 클래스
        EntryPacket entryPacket;
        ChatPacket chatPacket;
        PositionPacket positionPacket;
        GameReadyPacket gameReadyPacket;
        StartQueuePacket startQueuePacket;

        public MultiServerRec(Socket socket) {
            this.socket = socket;
            try {
                os = socket.getOutputStream();
                is = socket.getInputStream();
                oos = new ObjectOutputStream(os);
                ois = new ObjectInputStream(is);
            } catch (Exception e) {
                System.out.println("예외:" + e);
            }
        }

        @Override
        public void run() { //쓰레드를 사용하기 위해서 run()메서드 재정의

            try {

                System.out.println("User 한 명이 접속을 시도합니다.");

                while (ois == null) {
                    if(ois != null)
                        break;
                    else{
                        ois = new ObjectInputStream(is);
                    }
                }

                while (ois != null) {

                    HeaderPacket headerPacket = (HeaderPacket) ois.readObject();
                    byte requestCode = headerPacket.getRequestCode();
                    System.out.println("◎서버 패킷 받음 : 패킷 코드 " + requestCode);

                    switch (requestCode) {
                        case 0: // 입장 패킷
                            entryPacket = (EntryPacket) headerPacket;
                            usernick = entryPacket.getUserNick();
                            roomId = entryPacket.getRoomId();

                            getUserInRoom(roomId); // 입장 시 그 방 내의 유저 값 받아옴
                            userList.add(usernick); // 접속 유저 리스트에 추가

                            UserOosInfo uoi= new UserOosInfo(usernick, oos);
                            clientMap.put(usernick, uoi); //해쉬맵에 키를 name으로 출력스트림 객체를 저장.

                            sendAllEntryMsg(roomId, usernick, 0);
                            toServer(usernick, roomId, 0);

                            //System.out.println("총 접속자 수 : " + clientMap.size() );

                            break;

                        case 1: // 퇴장 패킷
                            entryPacket = (EntryPacket) headerPacket;
                            usernick = entryPacket.getUserNick();
                            roomId = entryPacket.getRoomId();

                            clientMap.remove(usernick);

                            sendAllEntryMsg(roomId, usernick, 1);
                            toServer(usernick, roomId, 1);

                            socket.close();
                            break;

                        case 2: // CHAT 패킷
                            chatPacket = (ChatPacket) headerPacket;
                            usernick = chatPacket.getUserNick();
                            roomId = chatPacket.getRoomId();
                            chat = chatPacket.getChat();

                            sendAllChatMsg(roomId, usernick, chat);
                            break;

                        case 3: // POSITION 설정 패킷
                            positionPacket = (PositionPacket) headerPacket;
                            usernick = positionPacket.getUserNick();
                            roomId = positionPacket.getRoomId();
                            position = positionPacket.getPosition();

                            sendAllPositionMsg(roomId, usernick, position);
                            toServer(usernick, roomId, 3);
                            break;

                        case 4: // 게임준비 패킷
                            gameReadyPacket = (GameReadyPacket) headerPacket;
                            roomId = gameReadyPacket.getRoomId();
                            usernick = gameReadyPacket.getUserNick();

                            sendAllGameReadyMsg(roomId, usernick, 4);
                            toServer(usernick, roomId, 4);
                            break;

                        case 5: // 게임준비해제 패킷
                            gameReadyPacket = (GameReadyPacket) headerPacket;
                            roomId = gameReadyPacket.getRoomId();
                            usernick = gameReadyPacket.getUserNick();

                            sendAllGameReadyMsg(roomId, usernick, 5);
                            toServer(usernick, roomId, 5);
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

        //접속된 모든 클라이언트들에게 입장, 퇴장 메시지를 전달.
        public void sendAllEntryMsg(int roomId, String usernick, int kinds) {

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while (it.hasNext()) {
                try {
                    UserOosInfo uoi = (UserOosInfo) clientMap.get(it.next());
                    for(int i = 0; i < userList.size(); i ++) {
                        System.out.println("방 인원"+userList.get(i));
                        if(uoi.getNick().equals(userList.get(i))){
                            setPacketInfo(usernick, roomId);
                            EntryPacket entryPacket = new EntryPacket(roomId, usernick, dataLength, kinds);
                            ObjectOutputStream oos = uoi.getOos();
                            //ObjectOutputStream oos = (ObjectOutputStream) clientMap.get(it.next());
                            oos.writeObject(entryPacket);
                            oos.flush();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("예외:" + e);
                }
            }
        }

        //접속된 모든 클라이언트들에게 채팅 메시지를 전달.
        public void sendAllChatMsg(int roomId, String usernick, String chat) {

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while (it.hasNext()) {
                try {
                    UserOosInfo uoi = (UserOosInfo) clientMap.get(it.next());
                    for(int i = 0; i < userList.size(); i ++) {
                        System.out.println("방 인원"+userList.get(i));
                        if(uoi.getNick().equals(userList.get(i))){
                            setPacketInfo(usernick, chat);
                            ChatPacket chatPacket = new ChatPacket(roomId, usernick, chat, dataLength);
                            ObjectOutputStream oos = uoi.getOos();
                            oos.writeObject(chatPacket);
                            oos.flush();

                            System.out.println("▶채팅 : "+roomId + "번방 : " + usernick + "=>" + chat);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("예외:" + e);
                }
            }
        }

        //접속된 모든 클라이언트들에게 자리 설정 메시지 전달
        public void sendAllPositionMsg(int roomId, String usernick, int position) {

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while (it.hasNext()) {
                try {
                    UserOosInfo uoi = (UserOosInfo) clientMap.get(it.next());
                    for(int i = 0; i < userList.size(); i ++) {
                        System.out.println("방 인원"+userList.get(i));
                        if(uoi.getNick().equals(userList.get(i))){
                            setPacketInfo(usernick, roomId, position);
                            PositionPacket positionPacket = new PositionPacket(roomId, usernick, dataLength, position);
                            ObjectOutputStream oos = uoi.getOos();
                            oos.writeObject(positionPacket);
                            oos.flush();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("예외:" + e);
                }
            }
        }

        //접속된 모든 클라이언트들에게 게임 준비 또는 준비 취소 메시지 전달
        public void sendAllGameReadyMsg(int roomId, String usernick, int kinds) {

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while (it.hasNext()) {
                try {
                    UserOosInfo uoi = (UserOosInfo) clientMap.get(it.next());
                    for(int i = 0; i < userList.size(); i ++) {
                        System.out.println("방 인원"+userList.get(i));
                        if(uoi.getNick().equals(userList.get(i))){
                            setPacketInfo(usernick, roomId);
                            GameReadyPacket gameReadyPacket = new GameReadyPacket(roomId, usernick, dataLength, kinds);
                            ObjectOutputStream oos = uoi.getOos();
                            oos.writeObject(gameReadyPacket);
                            oos.flush();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("예외:" + e);
                }
            }
        }

        //접속된 모든 클라이언트들에게 게임 시작 메시지 전달
        public void sendAllGameStartMsg(int roomId, String[] usernick) {

            //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
            Iterator it = clientMap.keySet().iterator();

            while (it.hasNext()) {
                try {
                    UserOosInfo uoi = (UserOosInfo) clientMap.get(it.next());
                    for(int i = 0; i < userList.size(); i ++) {
                        System.out.println("방 인원"+userList.get(i));
                        if(uoi.getNick().equals(userList.get(i))){
                            setPacketInfo(usernick, roomId);
                            StartQueuePacket startQueuePacket = new StartQueuePacket(roomId, usernick, dataLength);
                            ObjectOutputStream oos = uoi.getOos();
                            oos.writeObject(startQueuePacket);
                            oos.flush();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("예외:" + e);
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

        // 채팅 시 Packet 설정
        private void setPacketInfo(String usernick, int roomId,  int position) {
            String packet_Data = roomId + "|" + usernick + "|" + position; // "룸id|유저닉네임|포지션" 으로 패킷 설정
            dataLength = packet_Data.length();
        }

        // 채팅 시 Packet 설정
        private void setPacketInfo(String[] usernick, int roomId) {
            String packet_Data = roomId + "|" + usernick + "|"; // "룸id|게임시작유저 닉네임" 으로 패킷 설정
            dataLength = packet_Data.length();
        }

        // 서버 DB 연동
        private void toServer(String nick, int roomId, int requestCode) {
            try {
                URL url = null;

                switch (requestCode) {
                    case 0:
                        url = new URL("http://zladnrms.vps.phps.kr/android2/roomcnt/enter_room.php");
                        break;
                    case 1:
                        url = new URL("http://zladnrms.vps.phps.kr/android2/roomcnt/exit_room.php");
                        break;
                    case 3:
                        url = new URL("http://zladnrms.vps.phps.kr/android2/content/select_position.php");
                        break;
                    case 4:
                        url = new URL("http://zladnrms.vps.phps.kr/android2/content/game_ready.php");
                        break;
                    case 5:
                        url = new URL("http://zladnrms.vps.phps.kr/android2/content/game_ready_cancel.php");
                        break;
                }

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setInstanceFollowRedirects(false); // 추가됨
                    conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                    StringBuffer buffer = new StringBuffer();
                    if(requestCode==0 || requestCode==1){
                        buffer.append("nick").append("=").append(nick).append("&");
                        buffer.append("roomId").append("=").append(roomId);
                    } else if(requestCode==3){
                        buffer.append("nick").append("=").append(nick).append("&");
                        buffer.append("roomId").append("=").append(roomId).append("&");
                        buffer.append("position").append("=").append(position);
                    } else if(requestCode==4 || requestCode==5){
                        buffer.append("nick").append("=").append(nick).append("&");
                        buffer.append("roomId").append("=").append(roomId);
                    }

                    OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();

                    // 보내기  &&  받기
                    //헤더 받는 부분

                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder builder = new StringBuilder();
                    String result;
                    while ((result = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        builder.append(result + "\n");
                    }

                    if (builder.toString().trim().equals("success")) {
                        switch (requestCode){
                            case 0:
                                System.out.println("◎입장 알림 : " + roomId + "번방, " + usernick + "입장했습니다.");
                                break;
                            case 1:
                                System.out.println("◎퇴장 알림 : " + roomId + "번방, " + usernick + "퇴장했습니다.");
                                break;
                            case 3:
                                System.out.println("◎자리 선택 : " + roomId + "번방, " + usernick + "자리 선택 : " + position);
                                break;
                            case 4:
                                System.out.println("◎게임 준비 : " + roomId + "번방");
                                break;
                            case 5:
                                System.out.println("◎게임 준비 해제 : " + roomId + "번방");
                                break;
                        }
                    } else if(builder.toString().trim().equals("failure")){
                        switch (requestCode){
                            case 0:
                                System.out.println("◎입장 오류 : " + roomId + "번방, " + usernick + " / 입장에 오류 생김. (서버 문제) 강제 퇴장 조치");
                                toServer(usernick, roomId, 1);
                                socket.close();
                                break;
                            case 1:
                                System.out.println("◎퇴장 오류 : " + roomId + "번방, " + usernick + " / 퇴장에 오류 생김. (서버 문제) 퇴장 재시도");
                                toServer(usernick, roomId, 1);
                                break;
                            case 3:
                                System.out.println("◎자리 선택 : " + roomId + "번방, " + usernick + " / 자리 선택에 오류 생김. (서버 문제) 자동 재선택 시작");
                                toServer(usernick, roomId, 3);
                                break;
                            case 4:
                                System.out.println("◎게임 준비 실패: " + roomId + "번방, " + usernick + " / 게임 준비 실패");
                                break;

                            case 5:
                                System.out.println("◎게임 준비 해제 실패: " + roomId + "번방, " + usernick + " / 게임 준비 해제 실패");
                                break;
                        }
                    } else { // 게임 시작하는 2인의 닉네임 받아옴
                        String _startUser = builder.toString().trim();
                        String[] startUser = _startUser.split("\\|");
                        sendAllGameStartMsg(roomId, startUser);
                        System.out.println("◎게임 시작 : " + roomId + "번방, " + startUser[0] + ", " + startUser[1] + " 게임시작");
                    }
                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 서버에서 방 내 유저 정보 가져오기
        private void getUserInRoom(int roomId) {
            try {

                URL url = new URL("http://zladnrms.vps.phps.kr/android2/content/get_user_in_room.php"); // 앨범 폴더의 dbname 폴더에 접근

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setInstanceFollowRedirects(false); // 추가됨
                    conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("roomId").append("=").append(roomId);

                    OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();

                    // 보내기  &&  받기
                    //헤더 받는 부분

                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    String result;
                    while ((result = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        if (!result.equals("")) {
                            userList.add(result);
                            System.out.println(result);
                        }
                    }
                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // 유저 접속 시 해당 유저의 OOS값을 저장해두어 소켓 연결 유지
    public class UserOosInfo{

        String nick;
        ObjectOutputStream oos;

        UserOosInfo(String _nick, ObjectOutputStream _oos){
            this.nick = _nick;
            this.oos = _oos;
        }

        public ObjectOutputStream getOos() {
            return oos;
        }

        public String getNick() {
            return nick;
        }
    }
}