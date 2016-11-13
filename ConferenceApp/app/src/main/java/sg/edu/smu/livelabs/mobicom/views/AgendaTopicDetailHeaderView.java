package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEventEntity;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;

/**
 * Created by smu on 18/4/16.
 */
public class AgendaTopicDetailHeaderView {
    @Bind(R.id.time_txt)
    public TextView timeTxt;
    @Bind(R.id.room_no_txt)
    public TextView roomNoTxt;
    @Bind(R.id.session_no_txt)
    public TextView sessionNoTxt;
    @Bind(R.id.session_title_txt)
    public TextView sessionTitleTxt;

    @Bind(R.id.pager_txt)
    public TextView paperNoTxt;
    @Bind(R.id.title_txt)
    public TextView titleTxt;
    @Bind(R.id.author_layout)
    public LinearLayout authorLayout;
    @Bind(R.id.author_txt)
    public TextView authorTxt;
    @Bind(R.id.authors_txt)
    public TextView authorsTxt;

    @Bind(R.id.rate_layout)
    public LinearLayout rateLayout;
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

    @Bind(R.id.rate_this_talk_txt)
    public TextView rateThisTalkTxt;
    @Bind(R.id.average_txt)
    public TextView averageTxt;
    @Bind(R.id.average_rate_txt)
    public TextView averageRateTxt;
    @Bind(R.id.quiz_txt)
    public TextView quizTxt;
    @Bind(R.id.pdf_image)
    public ImageView pdfImg;
    @Bind(R.id.epub_image)
    public ImageView epubImg;
    @Bind(R.id.quiz_image)
    public ImageView quizImg;

    @Bind(R.id.comment_header)
    public TextView commentHeader;

    @Bind(R.id.be_first_tv)
    public TextView beFirstTV;

    private Context context;

    public AgendaTopicDetailHeaderView(View view, Context context){
        this.context = context;
        ButterKnife.bind(this, view);
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setBoldTypeface(timeTxt);
        uiHelper.setBoldTypeface(sessionNoTxt);
        uiHelper.setBoldTypeface(sessionTitleTxt);
        uiHelper.setBoldTypeface(paperNoTxt);
        uiHelper.setBoldTypeface(titleTxt);
        uiHelper.setBoldTypeface(authorTxt);
        uiHelper.setBoldTypeface(beFirstTV);
        uiHelper.setTypeface( roomNoTxt, authorsTxt, rateThisTalkTxt,
                averageRateTxt, averageTxt, commentHeader, quizTxt);
        stars = new ImageView[]{star1, star2, star3, star4, star5};
    }

    public void loadData(AgendaEvent agendaEvent, EventEntity eventEntity, PaperEventEntity paperEventEntity){

        timeTxt.setText(agendaEvent.getStartTimeStr());
        roomNoTxt.setText(eventEntity.getRoom());
//        roomNoTxt.setVisibility(View.INVISIBLE);
        sessionNoTxt.setText(agendaEvent.getTitle());
        sessionTitleTxt.setText(agendaEvent.getDescription());
        paperNoTxt.setText(eventEntity.getTitle());
        titleTxt.setText(eventEntity.getDescription());
        String str = String.format("%.1f", eventEntity.getRating());
        averageRateTxt.setText(str);
        if (paperEventEntity != null){
            authorTxt.setVisibility(View.VISIBLE);
            authorsTxt.setText(paperEventEntity.getAuthors());
            if (paperEventEntity.getEpubLink() == null || paperEventEntity.getEpubLink().isEmpty()){
                epubImg.setVisibility(View.GONE);
            } else {
                epubImg.setVisibility(View.VISIBLE);
            }
            if (paperEventEntity.getPdfLink() == null || paperEventEntity.getPdfLink().isEmpty()){
                pdfImg.setVisibility(View.GONE);
            } else {
                pdfImg.setVisibility(View.VISIBLE);
            }
        } else {
            authorTxt.setVisibility(View.GONE);
            authorsTxt.setVisibility(View.GONE);
            pdfImg.setVisibility(View.GONE);
            epubImg.setVisibility(View.GONE);
        }


        if (AgendaService.RATING_QUIZ.equals(eventEntity.getRatingQuizStatus())){
            quizImg.setVisibility(View.VISIBLE);
            quizTxt.setVisibility(View.VISIBLE);
            rateLayout.setVisibility(View.VISIBLE);
        } else if (AgendaService.RATING.equals(eventEntity.getRatingQuizStatus())){
            quizImg.setVisibility(View.GONE);
            quizTxt.setVisibility(View.GONE);
            rateLayout.setVisibility(View.VISIBLE);
        }else if (AgendaService.QUIZ.equals(eventEntity.getRatingQuizStatus())){
            quizImg.setVisibility(View.VISIBLE);
            quizTxt.setVisibility(View.VISIBLE);
            rateLayout.setVisibility(View.GONE);
        }
        else{
            quizImg.setVisibility(View.GONE);
            quizTxt.setVisibility(View.GONE);
            rateLayout.setVisibility(View.GONE);
        }



        //overView //overView <a href=\'http://www.google.com\'>See more</a>
//        getView().overViewContentTxt.setText(Html.fromHtml(eventEntity.getOverview()));
//        TextView seemore = Textoo
//                .config(getView().overViewContentTxt)
//                .addLinksHandler(new LinksHandler() {
//                    @Override
//                    public boolean onClick(View view, String url) {
//                        if (url.endsWith(".pdf")) {
//                            Intent i = new Intent(Intent.ACTION_VIEW);
//                            i.setData(Uri.parse(url));
//                            i.setClassName("com.google.android.apps.docs", "com.google.android.apps.viewer.PdfViewerActivity");
//                            mainActivity.startActivity(i);
//                            return true;
//                        } else {
//                            return false;
//                        }
//                    }
//                })
//                .apply();

        //rate


    }

    public void addAuthor(String[] authors){
//        if (authors == null) return;
//        if (authors.length < 2){
//            authorTxt.setText("Author");
//        } else {
//            authorTxt.setText("Authors");
//        }
//        UIHelper uiHelper = UIHelper.getInstance();
//        for (String author: authors){
//            TextView textView = new TextView(context);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            textView.setLayoutParams(layoutParams);
//            author = author.trim();
//            textView.setText(author);
//            uiHelper.setTypeface(textView);
//            textView.setTextColor(ContextCompat.getColor(context, R.color.black));
//            authorLayout.addView(textView);
//        }

//    public void addAuthor(List<AttendeeEntity> attendeeEntities){
//        UIHelper uiHelper = UIHelper.getInstance();
//        int index = START_AUTHOR_INDEX;
//        for (AttendeeEntity entity: attendeeEntities){
//            TextView textView = new TextView(getContext());
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(0, 5, 5, 5);
//            textView.setLayoutParams(layoutParams);
//            textView.setText(entity.getName()+",");
//            textView.setPadding(0, 10, 10, 10);
//            if (ChatService.ACTIVE.equals(entity.getStatus())){
//                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
//                uiHelper.setItalicTypeface(textView);
//                textView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
//                textView.setClickable(true);
//                textView.setOnClickListener(presenter);
//                textView.setTag(index);
//            } else {
//                uiHelper.setTypeface(textView);
//                textView.setClickable(false);
//                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color));
//            }
//            authorLayout.addView(textView);
//            index++;
//        }

    }

}
