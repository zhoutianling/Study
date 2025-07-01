package com.zero.study.db

import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zero.study.AppStudy

private const val DB_LOG = "db_log"

/**
 * 数据库管理工具
 */
object DbManager {

    //数据库名
    private const val DATABASE_NAME: String = "db_room_study.db"

    //懒加载创建数据库
    val db: RoomDB by lazy {
        Room.databaseBuilder(AppStudy.appContext, RoomDB::class.java, DATABASE_NAME).addCallback(DbCreateCallBack)//增加回调监听
            .fallbackToDestructiveMigration(false).addMigrations(Migration_1_2)//修改表
            .addMigrations(Migration_2_3)//增加表T_CAR
            .addMigrations(Migration_3_4)//增加表T_DOG
            .addMigrations(Migration_4_5)//修改表T_DOG
            .addMigrations(Migration_5_6)//删除T_CAR
            .build()
    }

    private object DbCreateCallBack : RoomDatabase.Callback() {
        //第一次创建数据库时调用
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.e(DB_LOG, "first onCreate db version: " + db.version)
        }
    }

    /**
     * 数据库升级
     */
    private val Migration_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            Log.e(DB_LOG, "Migration_1_2: ${db.version}")
            //增加字段money
            db.execSQL("alter table t_user add money integer default  999999 not null")
        }
    }

    private val Migration_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            Log.e(DB_LOG, "Migration_2_3: ${db.version}")
            db.execSQL("CREATE TABLE IF NOT EXISTS t_car (id integer primary key autoincrement not null, name text not null)")
        }
    }
    private val Migration_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            Log.e(DB_LOG, "Migration_3_4: ${db.version}")
            db.execSQL("create table if not exists t_dog (id integer primary key autoincrement not null, ownId integer not null,name  text not null)")
        }
    }

    private val Migration_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            //增加字段money
            db.execSQL("alter table t_dog add age integer")
            Log.e(DB_LOG, "Migration_4_5: ${db.version}")
        }
    }
    private val Migration_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            //增加字段money
            db.execSQL("drop table t_book")
            Log.e(DB_LOG, "Migration_5_6: ${db.version}")
        }
    }
}