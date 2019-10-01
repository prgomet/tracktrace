package com.cometengine.tracktrace.database

import androidx.room.*
import com.cometengine.tracktrace.misc.currentTimeStamp
import com.cometengine.tracktrace.database.TrackingItem.Companion.TRANSIT

@Dao
interface TrackingItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: TrackingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(item: TrackingItem)

    @Query("DELETE FROM items WHERE id = :id")
    fun delete(id: String)

    @Query("UPDATE items SET upd = :upd WHERE id = :id")
    fun setUpd(id: String, upd: Long)

    @Query("UPDATE items SET status = :status WHERE id = :id")
    fun setStatus(id: String, status: Int)

    @Query("UPDATE items SET title = :title WHERE id = :id")
    fun setTitle(id: String, title: String)

    @Transaction
    fun updateItemStatus(id: String, status: Int) {
        setStatus(id, status)
        setUpd(id, currentTimeStamp)
    }

    @Transaction
    fun updateItemTitle(id: String, title: String) {
        setTitle(id, title)
        setUpd(id, currentTimeStamp)
    }

    @Query("SELECT id FROM items WHERE status = $TRANSIT")
    fun getTrackingItemsForSync(): List<String>

    @Query("SELECT id, title, created, upd, status FROM items WHERE id = :trackingId")
    fun getItem(trackingId: String): TrackingItem?
}