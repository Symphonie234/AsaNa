package ph.asana.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedResponse::class], version = 1, exportSchema = false)
abstract class AsaNaDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
