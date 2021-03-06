package sg.edu.smu.livelabs.mobicom.models.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import sg.edu.smu.livelabs.mobicom.models.data.InterestsEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "INTERESTS_ENTITY".
*/
public class InterestsEntityDao extends AbstractDao<InterestsEntity, Long> {

    public static final String TABLENAME = "INTERESTS_ENTITY";

    /**
     * Properties of entity InterestsEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Interest = new Property(1, String.class, "interest", false, "INTEREST");
        public final static Property LastUpdated = new Property(2, java.util.Date.class, "lastUpdated", false, "LAST_UPDATED");
    };


    public InterestsEntityDao(DaoConfig config) {
        super(config);
    }
    
    public InterestsEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"INTERESTS_ENTITY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ASC ," + // 0: id
                "\"INTEREST\" TEXT," + // 1: interest
                "\"LAST_UPDATED\" INTEGER);"); // 2: lastUpdated
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"INTERESTS_ENTITY\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, InterestsEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String interest = entity.getInterest();
        if (interest != null) {
            stmt.bindString(2, interest);
        }
 
        java.util.Date lastUpdated = entity.getLastUpdated();
        if (lastUpdated != null) {
            stmt.bindLong(3, lastUpdated.getTime());
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public InterestsEntity readEntity(Cursor cursor, int offset) {
        InterestsEntity entity = new InterestsEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // interest
            cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)) // lastUpdated
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, InterestsEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setInterest(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setLastUpdated(cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(InterestsEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(InterestsEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
