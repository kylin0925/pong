package ap.ky.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kylin25 on 2016/11/11.
 */

public class PongSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Game";
    private ScheduledExecutorService scheduledExecutorService;

    Paint white;
    Paint backgroud;
    Paint paintScore;
    Rect ball = new Rect(0,0,30,30);
    Rect paddle1 = new Rect(0,0,100,10);
    Rect paddle2 = new Rect(0,0,100,10);
    int x = 10;
    int y = 10;
    int width = 0;
    int height = 0;
    int BALL_SIZE = 30;
    int PADDLE_WIDTH = 200;
    int PADDLE_HEIGHT = 10;
    int MOVE_STEP = 10;
    int PADDLE_STEP = 150;
    int PADDLE_PAD = 20;
    int score = 0;
    ArrayList<Long> intervalTime = new ArrayList<Long>(20);
    public PongSurfaceView(Context context) {
        super(context);

        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        white = new Paint();
        white.setColor(Color.WHITE);

        backgroud = new Paint();
        backgroud.setColor(Color.BLACK);

        for(int i = 0 ;i<19;i++){
            intervalTime.add(System.currentTimeMillis());
        }
        paintScore = new Paint();
        paintScore.setTextSize(40);
        paintScore.setColor(Color.WHITE);
    }

    void drawScene(Canvas canvas){
        canvas.drawColor(Color.BLACK);
        canvas.drawLine(0,height/2,width,height/2,white);
        canvas.drawRect(paddle1,white);
        canvas.drawRect(paddle2,white);

    }
    void updateBall(Rect ball){

        if(ball.right > width){
            x = -MOVE_STEP;
        }
        if(ball.left < 0) {
            x = MOVE_STEP;
        }
        if(ball.top < 0){
            y = MOVE_STEP;
        }
        if(ball.bottom > height){
            y=-MOVE_STEP;
        }

    }
    void movePaddle(Rect paddle ,int step){
        if(step < 0) {
            if (paddle.left - PADDLE_STEP >= 0)
                paddle.offset(-PADDLE_STEP, 0);
            else {
                paddle.offset(-paddle.left, 0);
            }
        }else{
            if(paddle.right + PADDLE_STEP <= width) {
                paddle.offset(PADDLE_STEP, 0);
            } else {
                paddle.offset(width-paddle.right, 0);
            }
        }
    }
    void aiMove(Rect paddle,Rect ball){
        if(ball.top < height/2 && y < 0){
            if(paddle.right< ball.left){
                Log.e(TAG,"move to right");
                movePaddle(paddle1,PADDLE_STEP);
            }
            if(paddle.left> ball.right){
                Log.e(TAG,"move to left");
                movePaddle(paddle1,-PADDLE_STEP);
            }
        }
    }
    void displayScore(Canvas canvas){

        canvas.drawText(String.format("Score %d", score), 10,height/2, paintScore);
    }
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        width = canvas.getWidth();
        height = canvas.getHeight();

        ball = new Rect(0,0,BALL_SIZE,BALL_SIZE);
        paddle1.top = PADDLE_PAD;
        paddle1.left = width/2 - PADDLE_WIDTH/2;
        paddle1.right = width/2 + PADDLE_WIDTH/2;
        paddle1.bottom =PADDLE_PAD + PADDLE_HEIGHT;


        paddle2.top = height - PADDLE_HEIGHT - PADDLE_PAD;
        paddle2.left = width/2 - PADDLE_WIDTH/2;
        paddle2.right = width/2 + PADDLE_WIDTH/2;
        paddle2.bottom = height - PADDLE_PAD;

        ball.offsetTo(width/2,height/2);

        surfaceHolder.unlockCanvasAndPost(canvas);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                intervalTime.add(System.currentTimeMillis());
                float fps = 20000 / (intervalTime.get(19) - intervalTime.get(0));
                intervalTime.remove(0);
                Canvas canvas = surfaceHolder.lockCanvas();
                drawScene(canvas);

                updateBall(ball);
                canvas.drawRect(ball,white);

                ball.offset(x, y);
                aiMove(paddle1,ball);
                if(ball.intersects(ball,paddle2)){
                    Log.e(TAG,"hit paddle2");
                    y = -y;
                    //Log.e(TAG,"width " + ball.width() + " , height " + ball.height() + " x " + ball.top + " " + ball.left);
                    score += 1;
                }
                if(ball.intersects(ball,paddle1)){
                    Log.e(TAG,"hit paddle2");
                    y = -y;

                }
                //update score
                if(ball.top <=0)
                    score +=5;
                if(ball.bottom >= height)
                    score = 0;
                displayScore(canvas);
                canvas.drawText(String.format("%.1f fps", fps), 0, 24, white);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        },1000,16, TimeUnit.MILLISECONDS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        scheduledExecutorService.shutdown();
        surfaceHolder.removeCallback(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x,y;
            x = event.getX();
            y = event.getY();
            Log.e(TAG,"x " + x + "  y " + y + " width " + width);
            if(x < width/2)
               movePaddle(paddle2,-PADDLE_STEP);
            else {
               movePaddle(paddle2,PADDLE_STEP);
            }

        }
        return super.onTouchEvent(event);
    }
}
