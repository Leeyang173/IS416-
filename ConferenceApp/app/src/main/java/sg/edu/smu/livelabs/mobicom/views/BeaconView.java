package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.BeaconPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BeaconScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(BeaconPresenter.class)
public class BeaconView extends LinearLayout {
    @Inject
    public BeaconPresenter presenter;

    @Bind(R.id.webView)
    public WebView webView;

    @Bind(R.id.beacon_list)
    public ListView beaconLV;

    @Bind(R.id.list_overlay)
    public View listOverlay;

    @Bind(R.id.close_webview)
    public Button closeWVBtn;

    @Bind(R.id.list_container)
    public RelativeLayout listContainer;

    @Bind(R.id.main_details_container)
    public RelativeLayout mmContainer;

    @Bind(R.id.webview_container)
    public RelativeLayout wvContainer;

    @Bind(R.id.progress_WV)
    public ProgressBar wvProgressBar;

    @Bind(R.id.scanning_msg_WV)
    public TextView wvMessageTV;

    @Bind(R.id.star_layout)
    public LinearLayout starLL;
    @Bind(R.id.star1)
    public ImageView star1;
    @Bind(R.id.star2)
    public ImageView star2;
    @Bind(R.id.star3)
    public ImageView star3;
    @Bind(R.id.star4)
    public ImageView star4;
    @Bind(R.id.star5)
    public ImageView star5;
    public ImageView[] stars;

    @Bind(R.id.pdf_image)
    public ImageView pdfIV;

    @Bind(R.id.calibrating)
    public LinearLayout calibratingLL;

    @Bind(R.id.star_container)
    public LinearLayout starContainer;

    @Bind(R.id.pdf_container)
    public LinearLayout pdfContainer;

    public BeaconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<BeaconScreenComponent>getDaggerComponent(context).inject(this);
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
        ButterKnife.bind(this);
        super.onFinishInflate();
        stars = new ImageView[]{star1, star2, star3, star4, star5};
    }
}
