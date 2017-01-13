package zladnrms.defytech.firsttcp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import zladnrms.defytech.firsttcp.Client.PositionRect;

/**
 * Created by Administrator on 2016-12-01.
 */

public class GameThread_backup extends Thread {

    private SurfaceHolder sf_holder;
    private GameSurfaceView sfv;
    private Context context;
    private boolean m_run = false;

    // 화면 변수 설정
    private Rect dst, src;
    private Bitmap imgBack; // 배경 이미지 Bitmap
    private int width, height; // 화면의 너비, 높이
    private int window_center_x, window_center_y; // 화면의 중심

    // 캐릭터 변수
    private Rect userRect;
    private Bitmap user, swordman; // 캐릭터 이미지 Bitmap

    // 오브젝트 변수 (유저 캐릭터 제외)
    private Bitmap object;

    // 그리기 변수
    private Canvas _canvas;

    // 포지션 Rect 저장 클래스,
    PositionRect positionRect;
    int userInfo_Position = 100;

    public GameThread_backup(SurfaceHolder _sf_holder, GameSurfaceView _sfv, Context _context) {

        this.sf_holder = _sf_holder;
        this.sfv = _sfv;
        this.context = _context;

        setSizeByWindow(); // 화면 크기에 따른 화면 중심점 설정
        setBitmapResources();

        dst = new Rect(0, 0, width, height); // ViewPort 의 크기
        src = new Rect(); // View Port용
        src.set(0, 0, width, height);

        //mPlayerInfo = new PlayerGameInfo(window_center_x, window_center_y);
        //mPlayer = new Player(window_center_x, window_center_y);

        userRect = new Rect(0, 0, user.getWidth(), user.getHeight()); // 유저 의 크기
        //mPlayer.setUserRect(0, 0, user.getWidth(), user.getHeight());
    }

    private void setSizeByWindow() {
        Point p = new Point();
        Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(p);

        // 화면 크기
        width = p.x;
        height = p.y;

        Log.d("게임 LOG", "화면 너비 : " + width + ", 화면 높이 :" + height);

        // 화면 중심점
        window_center_x = width / 2;
        window_center_y = height / 2;

        Log.d("게임 LOG", "화면 중심점 : " + window_center_x + ", " + window_center_y);
    }

    private void setBitmapResources() {
        Resources res = context.getResources();

        // 배경 이미지 불러오기
        imgBack = BitmapFactory.decodeResource(res, R.drawable.background);
        imgBack = Bitmap.createScaledBitmap(imgBack, width * 2, height * 2, true);

        // 유저 이미지 불러오기 및 중심점 찾기
        user = BitmapFactory.decodeResource(res, R.drawable.user);
        swordman = BitmapFactory.decodeResource(res, R.drawable.swordman);

        // 오브젝트 이미지 불러오기 및 중심점 찾기
        //object = BitmapFactory.decodeResource(res, R.drawable.plate_0);
    }

    // Position 절대좌표 저장된 클래스 받아오기
    public void setPositionRect(PositionRect positionRect) {
        this.positionRect = positionRect;
    }

    public void setPositionWhenStart(int position){
        userInfo_Position = position;
    }

    // 포지션 클릭 시
    public void clickPosition(int _pos) {

    }

    // 게임 시작 시 유저 세팅
    public void setUser(int position){
        Rect pos = positionRect.getRect(position);
        _canvas.drawBitmap(user, null, pos, null);

        System.out.println("렉트값 :" + pos.left + ", " + pos.top + ", " + pos.right + ", " + pos.bottom);
    }

    public void setRunning(boolean _m_run) {
        this.m_run = _m_run;
    }

    @SuppressLint("WrongCall")
    public void run() {
        while (m_run) {
            _canvas = null;
            try {
                _canvas = sf_holder.lockCanvas(null);
                synchronized (sf_holder) {
                    if(userInfo_Position!=100) {
                        setUser(userInfo_Position);
                    }
                    _canvas.drawBitmap(imgBack, src, dst, null); // 전체 맵 변화
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (_canvas != null) {
                    sf_holder.unlockCanvasAndPost(_canvas);
                }
            }
        }
    }
}
