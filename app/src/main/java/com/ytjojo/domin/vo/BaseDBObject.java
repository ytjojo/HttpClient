package com.ytjojo.domin.vo;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Administrator on 2016/3/25 0025.
 */
public class BaseDBObject extends BaseModel {
    @Column
    public long addToDatabaseTime;

    @Column
    public long dateBaseValidSecond;

    @Column
    public long dateBaseFetchCount;

    @Column
    public long lastDatabaseUpdateTime;


}
