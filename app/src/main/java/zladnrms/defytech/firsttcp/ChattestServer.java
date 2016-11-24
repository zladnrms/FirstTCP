package zladnrms.defytech.firsttcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/*
 * 다중 채팅 구현 파일. Linux 위에 올려놓는다. 그리고 javac ChatServer.java를 통해 class 파일로 컴파일하고, java ChatServer로 실행하여 놓는다.
 */
public class ChattestServer {

    HashMap clientMap; // 채팅 목록
    ServerSocket serverSocket = null;
    Socket socket = null;

    // 날짜 계산
    Calendar c;
    SimpleDateFormat sdf;

    //main메서드
    public static void main(String[] args) {
        ChattestServer ms = new ChattestServer(); //서버객체 생성.
        ms.init();//실행.
    }//main()------

    //생성자
    public ChattestServer() {
        c = Calendar.getInstance();
        sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        clientMap = new HashMap(); //클라이언트의 출력스트림을 저장할 해쉬맵 생성.
        Collections.synchronizedMap(clientMap); //해쉬맵 동기화 설정.
    }//생성자----

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

    //접속된 모든 클라이언트들에게 메시지를 전달.
    public void sendAllMsg(String nick, String msg) {

        //출력스트림을 순차적으로 얻어와서 해당 메시지를 출력한다.
        Iterator it = clientMap.keySet().iterator();

        while (it.hasNext()) {
            try {
                System.out.println(nick + "=> " + msg);
                DataOutputStream it_out = (DataOutputStream) clientMap.get(it.next());
                it_out.writeUTF(msg);
            } catch (Exception e) {
                System.out.println("예외:" + e);
            }
        }
    }//sendAllMsg()-----------

    ////////////////////////////////////////////////////////////////////////
    //----// 내부 클래스 //--------//

    // 클라이언트로부터 읽어온 메시지를 다른 클라이언트(socket)에 보내는 역할을 하는 메서드
    class MultiServerRec extends Thread {

        Socket socket;
        InputStream in;
        OutputStream out;
        DataInputStream dis;
        DataOutputStream dos;

        //생성자.
        public MultiServerRec(Socket socket) {
            this.socket = socket;
            try {
                //Socket으로부터 입력스트림을 얻는다.
                in = socket.getInputStream();
                dis = new DataInputStream(socket.getInputStream());
                //Socket으로부터 출력스트림을 얻는다.
                out = socket.getOutputStream();
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                System.out.println("예외:" + e);
            }
        }//생성자 ------------

        @Override
        public void run() { //쓰레드를 사용하기 위해서 run()메서드 재정의

            try {

                sendAllMsg("User", "님이 입장하셨습니다.");
                //현재 객체가 가지고있는 소켓을 제외하고 다른 소켓(클라이언트)들에게 접속을 알림.
                clientMap.put("User", out); //해쉬맵에 키를 name으로 출력스트림 객체를 저장.

                System.out.println("현재 접속자 수는 " + clientMap.size() + "명 입니다.");

                while (dis != null) { //입력스트림이 null이 아니면 반복.
                    // 1byte 를 읽어 데이터의 길이를 얻는다.
                    byte[] data = new byte[1024];

                    int packetLength = dis.readInt();
                    int packetData = dis.read(data, 4, packetLength);

                    if (packetData > 0) {

                        System.out.println("패킷 길이" + packetLength);
                        String msg = new String(data, StandardCharsets.UTF_8);
                        System.out.println("넘어온 것 : " + msg);

                        String[] msgArr = msg.split("\\|"); // msgArr[0] = 닉네임, msgArr[1] = 채팅 내용
                        if (msgArr.length == 1) {
                            sendAllMsg(msgArr[0], "");
                        } else {
                            sendAllMsg(msgArr[0], msgArr[1]);//현재 소켓에서 읽어온메시지를 해쉬맵에 저장된 모든 출력스트림으로 보낸다.
                        }



                    }

                }//while()---------
            } catch (SocketException e) {
                clientMap.remove("User");
                sendAllMsg("User", "님이 퇴장하셨습니다.");
                System.out.println("현재 접속자 수 : " + clientMap.size() + "명");
            } catch (IOException e) {

            }
            /*
            finally{
                //예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
                //보통 종료하거나 나가면 java.net.SocketException: 예외발생
                clientMap.remove(name);
                sendAllMsg(name + "님이 퇴장하셨습니다.");
                System.out.println("현재 접속자 수는 "+clientMap.size()+"명 입니다.");
            }
             */
        }//run()------------
    }//class MultiServerRec-------------
    //////////////////////////////////////////////////////////////////////
}