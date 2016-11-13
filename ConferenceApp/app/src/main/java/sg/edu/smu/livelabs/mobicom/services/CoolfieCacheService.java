package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;

/**
 * Created by smu on 28/2/16.
 */
public class CoolfieCacheService extends GeneralService {
    private static final CoolfieCacheService instance = new CoolfieCacheService();
    public static CoolfieCacheService getInstance(){return instance;}
//    private CoolfieCacheEntityDao coolfieCacheEntityDao;
//    private CompCacheEntityDao compCacheEntityDao;
//    private CompUploadCacheEntityDao compUploadCacheEntityDao;

    public void init(Context context){
        this.context = context;
//        coolfieCacheEntityDao = DatabaseService.getInstance().getCoolfieCacheEntityDao();
//        compCacheEntityDao = DatabaseService.getInstance().getCompCacheEntityDao();
//        compUploadCacheEntityDao = DatabaseService.getInstance().getCompUploadCacheEntityDao();
    }
//
//    public void updateCoolfieCacheList(List<Selfie> selfies, boolean isLeaderboard){
//        List<CoolfieCacheEntity> coolfieCacheEntities = new ArrayList<CoolfieCacheEntity>();
////        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
//
//        String compId = "";
//        try{
//            for(Selfie s: selfies){
//                compId = s.promotionId;
//                CoolfieCacheEntity c = new CoolfieCacheEntity();
//                c.setCoolfieId(Long.parseLong(s.id));
//                c.setAvatarId(s.userAvatar);
//                c.setCompId(s.promotionId);
//                c.setCompName(s.promotionName);
//                c.setDescription(s.description);
//                c.setEmailId(s.email);
//                c.setImageId(s.imageId);
//                c.setInsertTime(s.createdTime);
//                c.setIsLeaderBoard(isLeaderboard);
//                c.setLastUpdate(s.lastUpdated);
//                c.setLikeStatus(s.likeStatus);
//                c.setName(s.username);
//                c.setReportCount(s.report);
//                c.setReportStatus(s.reportStatus);
//                c.setStatus(s.status);
//                c.setToken(s.token);
//                c.setUserId(s.userId);
//                c.setLikeCount(s.likes);
//                coolfieCacheEntities.add(c);
//            }
//
//            if(!compId.isEmpty())
//                deleteCompIdCache(compId, isLeaderboard);
//            coolfieCacheEntityDao.insertOrReplaceInTx(coolfieCacheEntities);
//        }
//        catch (Exception e){
//
//        }
//    }
//
//    public void deleteCompIdCache(String compId, boolean isLeaderboard){
//        List<CoolfieCacheEntity> coolfieCacheEntities = coolfieCacheEntityDao.queryBuilder().where(CoolfieCacheEntityDao.Properties.CompId.eq(compId),
//                CoolfieCacheEntityDao.Properties.IsLeaderBoard.eq(isLeaderboard)).build().list();
//
//        coolfieCacheEntityDao.deleteInTx(coolfieCacheEntities);
//    }
//
//    public List<CoolfieCacheEntity> getCoolfieCache(String compId, boolean isLeaderboard){
//        return  coolfieCacheEntityDao.queryBuilder().where(CoolfieCacheEntityDao.Properties.CompId.eq(compId),
//                CoolfieCacheEntityDao.Properties.IsLeaderBoard.eq(isLeaderboard)).build().list();
//
//    }
//
//    public void updateCompCacheList(List<EVAPromotionItem> promotions){
//        List<CompCacheEntity> compCacheEntities = new ArrayList<CompCacheEntity>();
//        try{
//            for(EVAPromotionItem p: promotions){
//                CompCacheEntity c = new CompCacheEntity();
//                c.setCompName(p.name);
//                c.setId(Long.parseLong(p.id));
//                c.setEndTime(p.endTime);
//                c.setImage(p.image);
//                c.setImageCount(p.imageCount);
//                c.setStartTime(p.startTime);
//                c.setStatus(p.status);
//                compCacheEntities.add(c);
//
//            }
//
//            compCacheEntityDao.deleteAll();
//            compCacheEntityDao.insertOrReplaceInTx(compCacheEntities);
//        }
//        catch (Exception e){
//
//        }
//    }
//
//    public List<CompCacheEntity> getCacheComp(){
//        return compCacheEntityDao.queryBuilder().orderDesc(CompCacheEntityDao.Properties.StartTime).build().list();
//    }
//
//
//    public void setUploadImage(Bitmap image, String title, String compId, String imageId){
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//
//        CompUploadCacheEntity cacheEntity = new CompUploadCacheEntity();
//        cacheEntity.setImage(byteArray);
//        cacheEntity.setTitle(title);
//        cacheEntity.setCompId(compId);
//        cacheEntity.setImageId(imageId);
//
//        compUploadCacheEntityDao.insertOrReplace(cacheEntity);
//    }
//
//    public void uploadCachedPhoto(MainActivity activity){
//        List<CompUploadCacheEntity> c = compUploadCacheEntityDao.queryBuilder().build().list();
//
//        for(final CompUploadCacheEntity cacheEntity: c){
//            try {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(cacheEntity.getImage(), 0, cacheEntity.getImage().length);
//                UploadFileService.getInstance().uploadPhoto(activity, cacheEntity.getImageId(), bitmap, new Action1<String>() {
//                    @Override
//                    public void call(String imageId) {
////                    UIHelper.getInstance().showProgressDialog(context, "Uploading...", false);
//                        EVAPromotionService.getInstance().postCachedPhoto(cacheEntity.getId(), imageId, cacheEntity.getTitle());
//                    }
//
//                });
//            }
//            catch (Exception e){}
//        }
//    }
//
//
//    public void deleteCachePhoto(Long id){
//        compUploadCacheEntityDao.deleteByKey(id);
//    }
}
