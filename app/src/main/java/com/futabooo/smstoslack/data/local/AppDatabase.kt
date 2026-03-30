package com.futabooo.smstoslack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.futabooo.smstoslack.data.local.dao.FilterRuleDao
import com.futabooo.smstoslack.data.local.dao.ForwardedMessageDao
import com.futabooo.smstoslack.data.local.entity.FilterRuleEntity
import com.futabooo.smstoslack.data.local.entity.ForwardedMessageEntity

@Database(
    entities = [ForwardedMessageEntity::class, FilterRuleEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun forwardedMessageDao(): ForwardedMessageDao
    abstract fun filterRuleDao(): FilterRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_to_slack.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
