package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.PaperEntity;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEntityDao;
import sg.edu.smu.livelabs.mobicom.net.api.AgendaApi;

/**
 * Created by smu on 15/3/16.
 */
public class PaperService extends GeneralService {
    private static final PaperService instance = new PaperService();
    public static PaperService getInstance(){return instance;}

    private AgendaApi agendaApi;
    private Bus bus;
    private Context context;
    private PaperEntityDao paperEntityDao;

    public void init(Context context, Bus bus, AgendaApi agendaApi){
        this.context = context;
        this.bus = bus;
        this.agendaApi = agendaApi;
        this.paperEntityDao = DatabaseService.getInstance().getPaperEntityDao();
    }

    public PaperEntity createPaper(String title, String author, String URL){
        PaperEntity paperEntity = new PaperEntity();
        paperEntity.setAuthor(author);
        paperEntity.setTitle(title);
        paperEntity.setUrl(URL);
        return paperEntity;
    }

    public void savePaper(PaperEntity paper){
        paperEntityDao.insert(paper);
    }

    public void savePapers(List<PaperEntity> papers){
        paperEntityDao.insertInTx(papers);
    }

    public List<PaperEntity> getPaper(){
        return  paperEntityDao.queryBuilder().list();
    }

}
