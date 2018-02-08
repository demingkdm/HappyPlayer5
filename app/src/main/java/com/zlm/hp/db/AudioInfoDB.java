package com.zlm.hp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.DownloadInfo;
import com.zlm.hp.utils.PingYinUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import base.utils.DateUtil;

/**
 * 音频数据库处理
 * Created by zhangliangming on 2017/8/5.
 */
public class AudioInfoDB extends SQLiteOpenHelper {

    private Context mContext;

    /**
     * 表名
     */
    public static final String TBL_NAME = "audioInfoTbl";

    /**
     * 建表语句
     */
    public static final String CREATE_TBL = "create table " + TBL_NAME + "("
            + "songName text," + "singerName text," + "hash text,"
            + "fileExt text," + "fileSize long," + "fileSizeText text,"
            + "filePath text," + "duration long," + "durationText text," + "downloadUrl text,"
            + "createTime text," + "status int," + "recent int," + "likes int,"
            + "type int," + "category text," + "childCategory text"
            + ")";


    private static AudioInfoDB _AudioInfoDB;

    public AudioInfoDB(Context context) {
        super(context, "hp_audioinfo.db", null, 3);
        this.mContext = context;
    }

    public static AudioInfoDB getAudioInfoDB(Context context) {
        if (_AudioInfoDB == null) {
            _AudioInfoDB = new AudioInfoDB(context);
        }
        return _AudioInfoDB;
    }

    /**
     * 获取ContentValues
     *
     * @param audioInfo
     */
    private ContentValues getContentValues(AudioInfo audioInfo) {

        ContentValues values = new ContentValues();
        //
        values.put("songName", audioInfo.getSongName());
        values.put("singerName", audioInfo.getSingerName());
        values.put("hash", audioInfo.getHash());
        values.put("fileExt", audioInfo.getFileExt());
        values.put("fileSize", audioInfo.getFileSize());
        values.put("fileSizeText", audioInfo.getFileSizeText());
        values.put("filePath", audioInfo.getFilePath());
        values.put("duration", audioInfo.getDuration());
        values.put("durationText", audioInfo.getDurationText());
        values.put("downloadUrl", audioInfo.getDownloadUrl());
        values.put("createTime", audioInfo.getCreateTime());
        values.put("status", audioInfo.getStatus());
        values.put("type", audioInfo.getType());
        values.put("recent", audioInfo.getRecent());
        values.put("likes", audioInfo.getLike());


        //获取索引
        String category = PingYinUtil.getPingYin(audioInfo.getSingerName())
                .toUpperCase();
        char cat = category.charAt(0);
        if (cat <= 'Z' && cat >= 'A') {
            audioInfo.setCategory(cat + "");
            audioInfo.setChildCategory(category);
        } else {
            audioInfo.setCategory("^");
            audioInfo.setChildCategory(category);
        }

        values.put("category", audioInfo.getCategory());
        values.put("childCategory", audioInfo.getChildCategory());

        return values;
    }

    /**
     * 添加
     *
     * @param audioInfo
     */
    public boolean add(AudioInfo audioInfo) {

        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);
        values.add(value);

        return insert(values);
    }

    /**
     * 添加s
     *
     * @param audioInfos
     */
    public boolean add(List<AudioInfo> audioInfos) {
        List<ContentValues> values = new ArrayList<ContentValues>();
        for (AudioInfo audioInfo : audioInfos) {
            values.add(getContentValues(audioInfo));
        }
        return insert(values);
    }

    /**
     * 插入数据
     *
     * @param values
     * @return
     */
    private boolean insert(List<ContentValues> values) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction(); // 手动设置开始事务

            for (ContentValues value : values) {

                db.insert(TBL_NAME, null, value);
            }
            db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // 处理完成
        }
        return false;
    }

    public boolean update(AudioInfo audioInfo) {
        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);
        values.add(value);
        return update(values, audioInfo.getHash());
    }

    /**
     * 更新数据
     */
    private boolean update(List<ContentValues> values, String hash) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction(); // 手动设置开始事务
            for (ContentValues value : values) {
                db.update(TBL_NAME, value, "hash=?", new String[]{hash});
            }
            db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // 处理完成
        }
        return false;
    }

    /**
     * 删除hash对应的数据
     */
    public void delete(String hash) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "hash=?", new String[]{hash});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除type对应的数据
     *
     * @param type
     */
    public void delete(int type) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "type=?", new String[]{type + ""});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否存在
     *
     * @param hash
     * @return
     */
    public boolean isExists(String hash) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TBL_NAME, new String[]{},
                " hash=?", new String[]{hash}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * 删除表
     */
    public void deleteTab() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("drop table if exists " + TBL_NAME);
            db.execSQL(CREATE_TBL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取本地歌曲总数
     *
     * @return
     */
    public int getLocalAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.rawQuery("select count(*)from " + TBL_NAME
                + " WHERE type=? or ( type=? and status=? )", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 获取所有本地歌曲分类
     *
     * @return
     */
    public List<String> getAllLocalCategory() {

        // 第一个参数String：表名
        // 第二个参数String[]:要查询的列名
        // 第三个参数String：查询条件
        // 第四个参数String[]：查询条件的参数
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制
        // 第七个参数String：对查询的结果进行排序
        List<String> list = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.query(true, TBL_NAME, new String[]{"category"},
                "type=? or ( type=? and status=? )", args,
                null, null, "category asc , childCategory asc", null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("category")));
        }
        cursor.close();
        String baseCategory = "^";
        if (!list.contains(baseCategory)) {
            list.add(baseCategory);
        }
        return list;
    }

    /**
     * 获取分类下的歌曲
     *
     * @param category
     * @return
     */
    public List<Object> getLocalAudio(String category) {
        List<Object> list = new ArrayList<Object>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {category, AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.query(TBL_NAME, null,
                "category= ? and (type=? or ( type=? and status=? ))", args, null, null,
                "childCategory asc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 获取所有本地歌曲
     *
     * @return
     */
    public List<AudioInfo> getAllLocalAudio() {
        List<AudioInfo> list = new ArrayList<AudioInfo>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LOCAL + "", AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.query(TBL_NAME, null,
                "type=? or ( type=? and status=? )", args, null, null,
                "category asc ,childCategory asc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            File audioFile = new File(audioInfo.getFilePath());
            if (!audioFile.exists()) {
                continue;
            }
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 通过hash获取歌曲
     *
     * @param hash
     * @return
     */
    public AudioInfo getAudioInfoByHash(String hash) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TBL_NAME
                + " where hash=?", new String[]{hash + ""});
        if (!cursor.moveToNext()) {
            return null;
        }
        AudioInfo audioInfo = getAudioInfoFrom(cursor);
        cursor.close();
        return audioInfo;
    }

    /**
     * 获取数据
     *
     * @param cursor
     * @return
     */
    public AudioInfo getAudioInfoFrom(Cursor cursor) {
        AudioInfo audioInfo = new AudioInfo();

        audioInfo.setSongName(cursor.getString(cursor.getColumnIndex("songName")));
        audioInfo.setSingerName(cursor.getString(cursor.getColumnIndex("singerName")));
        audioInfo.setHash(cursor.getString(cursor.getColumnIndex("hash")));
        audioInfo.setFilePath(cursor.getString(cursor.getColumnIndex("filePath")));
        audioInfo.setFileExt(cursor.getString(cursor.getColumnIndex("fileExt")));
        audioInfo.setFileSize(cursor.getLong(cursor.getColumnIndex("fileSize")));
        audioInfo.setFileSizeText(cursor.getString(cursor.getColumnIndex("fileSizeText")));
        audioInfo.setDuration(cursor.getLong(cursor.getColumnIndex("duration")));
        audioInfo.setDurationText(cursor.getString(cursor.getColumnIndex("durationText")));
        audioInfo.setDownloadUrl(cursor.getString(cursor.getColumnIndex("downloadUrl")));
        audioInfo.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
        audioInfo.setStatus(cursor.getInt(cursor
                .getColumnIndex("status")));
        audioInfo.setType(cursor.getInt(cursor
                .getColumnIndex("type")));
        audioInfo.setRecent(cursor.getInt(cursor
                .getColumnIndex("recent")));
        audioInfo.setLike(cursor.getInt(cursor
                .getColumnIndex("likes")));
        audioInfo.setCategory(cursor.getString(cursor.getColumnIndex("category")));
        audioInfo.setChildCategory(cursor.getString(cursor.getColumnIndex("childCategory")));

        return audioInfo;
    }
    //、、、、、、、、、、、、、、、、、、、、、、、、、、、下载、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、


    /**
     * 判断网络歌曲是否在本地
     *
     * @param hash
     * @return
     */
    public boolean isNetAudioExists(String hash) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TBL_NAME, new String[]{},
                " hash=? and status=?", new String[]{hash, AudioInfo.FINISH + ""}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * 获取正在下载任务
     *
     * @return
     */
    public List<Object> getDownloadingAudio() {
        List<Object> list = new ArrayList<Object>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.DOWNLOAD + "", AudioInfo.INIT + "", AudioInfo.DOWNLOADING + ""};
        Cursor cursor = db.query(TBL_NAME, null,
                "type=? and (status=? or status=?)", args, null, null,
                "createTime desc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            File audioFile = new File(audioInfo.getFilePath());
            if (!audioFile.exists() && audioInfo.getStatus() == AudioInfo.FINISH) {
                continue;
            }
            //
            DownloadInfo downloadInfo = new DownloadInfo();
            downloadInfo.setAudioInfo(audioInfo);
            //获取下载进度
            DownloadInfoDB.getAudioInfoDB(mContext).getDownloadInfoByHash(downloadInfo, audioInfo.getHash());

            list.add(downloadInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 获取已下载
     *
     * @return
     */
    public List<Object> getDownloadedAudio() {
        List<Object> list = new ArrayList<Object>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.DOWNLOAD + "", AudioInfo.FINISH + ""};
        Cursor cursor = db.query(TBL_NAME, null,
                "type=? and status=?", args, null, null,
                "createTime desc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            File audioFile = new File(audioInfo.getFilePath());
            if (!audioFile.exists() && audioInfo.getStatus() == AudioInfo.FINISH) {
                continue;
            }
            //
            DownloadInfo downloadInfo = new DownloadInfo();
            downloadInfo.setAudioInfo(audioInfo);
            //获取下载进度
            DownloadInfoDB.getAudioInfoDB(mContext).getDownloadInfoByHash(downloadInfo, audioInfo.getHash());

            list.add(downloadInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 更新
     *
     * @param hash
     */
    public void updateDonwloadInfo(String hash, int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);

        try {
            db.update(TBL_NAME, values,
                    "type=? and hash=? ",
                    new String[]{AudioInfo.DOWNLOAD + "", hash});
        } catch (SQLException e) {
            Log.i("error", "update failed");
        }
    }

    /**
     * 获取下载歌曲个数
     *
     * @return
     */
    public int getDonwloadAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.DOWNLOAD + ""};
        Cursor cursor = db.rawQuery("select count(*)from " + TBL_NAME
                + " WHERE type=? ", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }


    /**
     * 删除hash对应的数据
     */
    public void deleteDonwloadAudio(String hash) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "hash=? and type=?", new String[]{hash, AudioInfo.DOWNLOAD + ""});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加
     *
     * @param audioInfo
     */
    public boolean addDonwloadAudio(AudioInfo audioInfo) {

        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);

        value.put("type", AudioInfo.DOWNLOAD);
        value.put("status", AudioInfo.INIT);

        values.add(value);

        return insert(values);
    }

    /////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\///最近、喜欢///////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\/////////////////////

    /**
     * 添加
     *
     * @param audioInfo
     */
    public boolean addRecentAudio(AudioInfo audioInfo) {
        int type = audioInfo.getType();
        int recent;
        if (type == AudioInfo.NET) {
            recent = AudioInfo.RECENT_NET;
        } else if (type == AudioInfo.THIIRDNET) {
            recent = AudioInfo.RECENT_THIIRDNET;
        } else {
            recent = AudioInfo.RECENT_LOCAL;
        }
        //更新创建时间
        audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);
        value.put("recent", recent);

        values.add(value);

        return insert(values);
    }

    public boolean addLikeAudio(AudioInfo audioInfo) {
        int type = audioInfo.getType();
        int like;
        if (type == AudioInfo.NET) {
            like = AudioInfo.LIKE_NET;
        } else if (type == AudioInfo.THIIRDNET) {
            like = AudioInfo.LIKE_THIIRDNET;
        } else {
            like = AudioInfo.LIKE_LOCAL;
        }

        //更新创建时间
        audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(audioInfo);
        value.put("likes", like);

        values.add(value);

        return insert(values);
    }

    /**
     * 是否存在
     *
     * @param audioInfo
     * @return
     */
    public boolean isRecentExists(AudioInfo audioInfo) {

        String hash = audioInfo.getHash();
        int recent = audioInfo.getRecent();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TBL_NAME, new String[]{},
                " hash=? and recent=?", new String[]{hash, recent + ""}, null, null, null);
        if (cursor.moveToNext()) {
            int recent1 = cursor.getInt(cursor
                    .getColumnIndex("recent"));
            cursor.close();
            return recent1 != -1;
        } else {//查不到
            cursor.close();
            return false;
        }
//        if (!cursor.moveToNext()) {
//            int recent1 = cursor.getInt(cursor
//                    .getColumnIndex("recent"));
//            cursor.close();
//            return false;
//        }
//        cursor.close();
//        return true;
    }

    /**
     * 是否存在
     *
     * @param audioInfo
     * @return
     */
    public boolean isLikeExists(AudioInfo audioInfo) {

        String hash = audioInfo.getHash();
        int like = audioInfo.getLike();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TBL_NAME, new String[]{},
                " hash=? and likes=?", new String[]{hash, like + ""}, null, null, null);
        if (cursor.moveToNext()) {
            int likes = cursor.getInt(cursor
                    .getColumnIndex("likes"));
            cursor.close();
            return likes != -1;
        } else {//查不到
            cursor.close();
            return false;
        }

//        if (!cursor.moveToNext()) {
//            int likes = cursor.getInt(cursor
//                    .getColumnIndex("likes"));
//            cursor.close();
//            return false;
//        }
//        cursor.close();
//        return true;
    }

    /**
     * 删除hash对应的数据
     */
    public void deleteRecenteAudio(AudioInfo audioInfo) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "hash=? and recent=?",
                    new String[]{audioInfo.getHash(), audioInfo.getRecent() + ""});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteLikeAudio(AudioInfo audioInfo) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "hash=? and likes=?",
                    new String[]{audioInfo.getHash(), audioInfo.getLike() + ""});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新最近歌曲数据
     *
     * @param audioInfo
     * @return
     */
    public boolean updateRecentAudio(AudioInfo audioInfo, boolean isRecent) {
        int type = audioInfo.getType();
        if(isRecent) {
            if (type == AudioInfo.LOCAL) {
                audioInfo.setRecent(AudioInfo.RECENT_LOCAL);
            } else if (type == AudioInfo.NET) {
                audioInfo.setRecent(AudioInfo.RECENT_NET);
            } else if (type == AudioInfo.THIIRDNET) {
                audioInfo.setRecent(AudioInfo.RECENT_THIIRDNET);
            }
        }else {
            audioInfo.setRecent(AudioInfo.RECENT_DEFAULT);
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("createTime", DateUtil.parseDateToString(new Date()));
        values.put("recent", audioInfo.getRecent());

        try {
            db.update(TBL_NAME, values, "hash=?",
                    new String[]{audioInfo.getHash()});
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新喜欢歌曲数据
     *
     * @param audioInfo
     * @return
     */
    public boolean updateLikeAudio(AudioInfo audioInfo, boolean isLike) {
        int type = audioInfo.getType();
        if(isLike) {
            if (type == AudioInfo.LOCAL) {
                audioInfo.setLike(AudioInfo.LIKE_LOCAL);
            } else if (type == AudioInfo.NET) {
                audioInfo.setLike(AudioInfo.LIKE_NET);
            } else if (type == AudioInfo.THIIRDNET) {
                audioInfo.setLike(AudioInfo.LIKE_THIIRDNET);
            }
        }else {
            audioInfo.setLike(AudioInfo.LIKE_DEFAULT);
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("createTime", DateUtil.parseDateToString(new Date()));
        values.put("likes", audioInfo.getLike());

        try {
            db.update(TBL_NAME, values, "hash=?",
                    new String[]{audioInfo.getHash()});
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取最近歌曲总数
     *
     * @return
     */
    public int getRecentAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.RECENT_LOCAL + "", AudioInfo.RECENT_NET + "", AudioInfo.RECENT_THIIRDNET + ""};
        Cursor cursor = db.rawQuery("select count(*)from " + TBL_NAME
                + " WHERE recent=? or recent=? or recent=? ", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 获取喜欢歌曲总数
     *
     * @return
     */
    public int getLikeAudioCount() {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LIKE_LOCAL + "", AudioInfo.LIKE_NET + "", AudioInfo.LIKE_THIIRDNET + ""};
        Cursor cursor = db.rawQuery("select count(*)from " + TBL_NAME
                + " WHERE likes=? or likes=? or likes=? ", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 获取最近所有歌曲
     *
     * @return
     */
    public List<AudioInfo> getAllRecentAudio() {
        List<AudioInfo> list = new ArrayList<AudioInfo>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.RECENT_LOCAL + "", AudioInfo.RECENT_NET + "", AudioInfo.RECENT_THIIRDNET + ""};
        Cursor cursor = db.query(TBL_NAME, null,
                "recent=? or recent=? or recent=?", args, null, null,
                "createTime desc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            //
            if (audioInfo.getType() == AudioInfo.RECENT_LOCAL) {
                audioInfo.setType(AudioInfo.LOCAL);
            } else {
                audioInfo.setType(AudioInfo.NET);
            }
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 获取所有喜欢的歌曲列表
     *
     * @return
     */
    public List<AudioInfo> getAllLikeAudio() {
        List<AudioInfo> list = new ArrayList<AudioInfo>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {AudioInfo.LIKE_LOCAL + "", AudioInfo.LIKE_NET + "", AudioInfo.LIKE_THIIRDNET + ""};
        Cursor cursor = db.query(TBL_NAME, null,
                "likes=? or likes=? or likes=?", args, null, null,
                "createTime desc", null);
        while (cursor.moveToNext()) {
            AudioInfo audioInfo = getAudioInfoFrom(cursor);
            //
            if (audioInfo.getType() == AudioInfo.LIKE_LOCAL) {
                audioInfo.setType(AudioInfo.LOCAL);
            } else {
                audioInfo.setType(AudioInfo.NET);
            }
            list.add(audioInfo);
        }
        cursor.close();
        return list;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TBL);
        } catch (SQLException e) {
            Log.i("error", "create table failed");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        try {
            db.execSQL("drop table if exists " + TBL_NAME);
        } catch (SQLException e) {
            Log.i("error", "drop table failed");
        }
        onCreate(db);
    }
}
