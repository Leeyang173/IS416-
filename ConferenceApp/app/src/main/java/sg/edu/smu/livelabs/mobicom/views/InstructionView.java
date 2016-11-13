package sg.edu.smu.livelabs.mobicom.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;

/**
 * Created by smu on 28/3/16.
 */
public class InstructionView extends FrameLayout implements View.OnClickListener {
    private Context context;
    private TextView titleTxt;
    private TextView descriptionTxt;
    private CircleImageView closeBtn;
    private ImageView personImg;
    private View overlayView;

    private CharSequence description;
    private int mIndex;
    private long mDelay = 500;
    private TextToSpeech textToSpeech;

    public InstructionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public InstructionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InstructionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public InstructionView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.instruction_view, this, true);
        titleTxt = (TextView) findViewById(R.id.title_txt);
        descriptionTxt = (TextView) findViewById(R.id.description_txt);
        closeBtn = (CircleImageView) findViewById(R.id.close_btn);
        personImg = (ImageView) findViewById(R.id.person_img);
        overlayView = findViewById(R.id.overlay_view);
        UIHelper.getInstance().setBoldTypeface(titleTxt);
        UIHelper.getInstance().setTypeface(descriptionTxt);
        closeBtn.setOnClickListener(this);
        overlayView.setOnClickListener(this);
//        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if(status != TextToSpeech.ERROR) {
//                    textToSpeech.setLanguage(Locale.UK);
//                }
//            }
//        });
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            descriptionTxt.setText(description.subSequence(0, mIndex++));
            if(mIndex <= description.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
                if (mIndex % 2 == 0){
                    personImg.setImageResource(R.drawable.person2);
                } else {
                    personImg.setImageResource(R.drawable.person);
                }
            }
        }
    };

    public void setContent(String title, String description, int writerDelay){
        titleTxt.setText(title);
        this.description = description;
        mDelay = writerDelay;
        mIndex = 0;
        setVisibility(VISIBLE);
        descriptionTxt.setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
//        App.getInstance().getMainActivity().getSpeaker().speak(description, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    public void onClick(View v) {
        setVisibility(GONE);
    }
}
