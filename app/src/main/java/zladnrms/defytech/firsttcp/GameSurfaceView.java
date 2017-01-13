package zladnrms.defytech.firsttcp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import zladnrms.defytech.firsttcp.Client.PositionRect;

/**
 * Created by Administrator on 2016-12-01.
 */

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread_backup gameThread;

    public GameSurfaceView(Context context) {
        super(context);

        System.out.println("1번째");
        getHolder().addCallback(this);
        gameThread = new GameThread_backup(getHolder(), this, context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        System.out.println("2번째");
        getHolder().addCallback(this);
        gameThread = new GameThread_backup(getHolder(), this, context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        System.out.println("3번째");
        getHolder().addCallback(this);
        gameThread = new GameThread_backup(getHolder(), this, context);
    }

    public void setPositionRect(PositionRect positionRect){
        gameThread.setPositionRect(positionRect);
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
