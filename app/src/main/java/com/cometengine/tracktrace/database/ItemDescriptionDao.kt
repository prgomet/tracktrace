package com.cometengine.tracktrace.database

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface ItemDescriptionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(list: ArrayList<ItemDescription>): List<Long>

    @Query("DELETE FROM description WHERE trackingId IN(:list)")
    fun delete(list: List<String>)

    @Query("DELETE FROM description WHERE trackingId = :id")
    fun delete(id: String)

    @Query("SELECT ${TrackingItemDescription.SELECT_ALT} FROM description LEFT JOIN items ON items.id = description.trackingId GROUP BY description.trackingId ORDER BY description.created DESC")
    fun getAllItems(): DataSource.Factory<Int, TrackingItemDescription>

    @Query("SELECT ${TrackingItemDescription.SELECT} FROM description LEFT JOIN items ON items.id = description.trackingId WHERE items.id = :trackingId ORDER BY description.created DESC")
    fun getItemsFor(trackingId: String): DataSource.Factory<Int, TrackingItemDescription>

    @Query("SELECT id, trackingId, location, description, created, seen FROM description WHERE trackingId = :trackingId ORDER BY created DESC LIMIT :limit")
    fun getTracking(trackingId: String, limit: Int): List<ItemDescription>

    @Query("SELECT count(id) FROM description WHERE trackingId = :trackingId")
    fun countTrackingItems(trackingId: String): Int

    @Query("UPDATE description SET seen = :seen WHERE seen == :oldSeen")
    fun updateSeen(seen: Long, oldSeen: Long)
}