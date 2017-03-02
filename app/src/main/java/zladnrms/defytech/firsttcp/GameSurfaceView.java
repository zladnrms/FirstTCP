package zladnrms.defytech.firsttcp;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import zladnrms.defytech.firsttcp.Client.PosClass;

/**
 * Created by Administrator on 2016-12-01.
 */

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;

    public GameSurfaceView(Context context) {
        super(context);

        System.out.println("1번째");
        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this, context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        System.out.println("2번째");
        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this, context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        System.out.println("3번째");
        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this, context);
    }

    public void setPositionList(ArrayList<PosClass> posList){
        gameThread.setPositionList(posList);
    }

    public void clickPosition (int _pos) {
        gameThread.clickPosition(_pos);
    }

    public void setPositionWhenStart(int position){
        gameThread.setPositionWhenStart(position);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.boss);
        //canvas.drawColor(Color.WHITE);
        //canvas.drawBitmap(bm, 40, 40, null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while(retry){
            try{
                gameThread.join();
                retry = false;
            } catch (InterruptedException e){

            }
        }
    }
}
