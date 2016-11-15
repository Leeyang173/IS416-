package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.NavigationPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.NavigationScreenComponent;

/**
 * Created by Jerms on 14/11/16.
 */
@AutoInjector(NavigationPresenter.class)
public class NavigationView extends RelativeLayout {

    @Inject
    public NavigationPresenter presenter;

    @Bind(R.id.message)
    public TextView messageTV;
    @Bind(R.id.arButton)
    public Button arButton;
    @Bind(R.id.locationSpinner)
    public Spinner locDDL;
    @Bind(R.id.textView)
    public TextView tv;
    @Bind(R.id.mapView)
    public ImageView map;

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<NavigationScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        presenter.dropView(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        ArrayList<Path> paths = new ArrayList<Path>();
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);

        //keep track of initialPoint and lastPoint to draw shapes
        int initialPointX = 0, initialPointY = 0, endPointX = 0, endPointY = 0;
        int[] nodesFound = new int[20];
        try {
            ArrayList<int[]> result = NavigationPresenter.test();
            //System.out.println(NavigationPresenter.test());
            for(int i = 0; i < result.size(); i++){
                nodesFound = result.get(0);
            }
            //nodesFound = result.get(0);
            //get from graph.toString();
            //Integer[] nodesFound = new Integer[]{13,12,11,10,9,8,26,44,62,80,98,116};
            //for each waypoint found, draw a line

            //move unit size (pixels)
            int xMoveUnit = 46;
            int yMoveUnit = 35;

            //find Level Map's origin point (probably top left hand corner of image)
            int mapPositionX = 218;
            int mapPositionY = 680;

            //take arrayHeight/Width from EdgeGenerator class
            int arrayWidth = NavigationPresenter.getWidth();

            for(int i = 0; i < nodesFound.length - 1; i++){
                int node1 = nodesFound[i];
                int node2 = nodesFound[i+1];

                //find map's origin point, factor in original position (probably top left hand corner of image) + node's position                int originNodeX = node1/arrayHeight;
                int originNodeX = node1%arrayWidth;
                int originNodeY = node1/arrayWidth;
                System.out.println("originNode:" + node1);
                System.out.println("endNodeY:" + node2);
                int startX = mapPositionX + originNodeX * xMoveUnit;
                int startY = mapPositionY + originNodeY * yMoveUnit;

                System.out.println("origin node's X movement multiplicator:" + originNodeX);
                System.out.println("origin node's Y movement multiplicator:" + originNodeY);


                //destination x,y
                int endNodeX = node2%arrayWidth;
                int endNodeY = node2/arrayWidth;

                System.out.println("end node's X movement multiplicator:" + endNodeX);
                System.out.println("end node's Y movement multiplicator:" + endNodeY);

                int endX = mapPositionX + endNodeX * xMoveUnit;
                int endY = mapPositionY + endNodeY * yMoveUnit;;


                Path path = new Path();
                System.out.println("StartX:" + startX + ",StartY:" + startY);
                System.out.println("EndX:" + endX + ",EndY:" + endY);
                path.moveTo(startX, startY);
                path.lineTo(endX, endY);
                path.close();

                //keep track of drawing initial point Circle, and endPoint Circle
                if(i == 0){
                    initialPointX = startX;
                    initialPointY = startY;
                }
                if(i == nodesFound.length - 2){
                    endPointX = endX;
                    endPointY = endY;
                }
                paths.add(path);
            }


            Paint paint = new Paint();

            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawCircle(initialPointX, initialPointY, 25, paint);

            paint.setColor(Color.RED);
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(endPointX, endPointY, 25, paint);


            paint.setDither(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(10);

            for(Path p:paths){
                canvas.drawPath(p, paint);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

