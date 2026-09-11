package com.ak.financeai.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FinanceDb(context: Context) : SQLiteOpenHelper(context, "ak_finance.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE sms_records (id INTEGER PRIMARY KEY AUTOINCREMENT,sender TEXT NOT NULL,bank TEXT,type TEXT NOT NULL,amount INTEGER,body TEXT NOT NULL,sanitized_body TEXT NOT NULL,confidence REAL NOT NULL,created_at INTEGER NOT NULL,reviewed INTEGER NOT NULL DEFAULT 0,linked_transaction_id INTEGER)")
        db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT,bank TEXT,amount INTEGER,status TEXT NOT NULL,created_at INTEGER NOT NULL,source_sms_id INTEGER)")
        db.execSQL("CREATE TABLE feedback (id INTEGER PRIMARY KEY AUTOINCREMENT,sms_id INTEGER NOT NULL,label TEXT NOT NULL,created_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE sms_records ADD COLUMN reviewed INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE sms_records ADD COLUMN linked_transaction_id INTEGER")
        }
    }
    fun insertSms(r: SmsRecord): Long {
        val v=ContentValues().apply {
            put("sender",r.sender); put("bank",r.bank); put("type",r.type.name)
            if(r.amount==null) putNull("amount") else put("amount",r.amount)
            put("body",r.body); put("sanitized_body",r.sanitizedBody); put("confidence",r.confidence)
            put("created_at",r.createdAt); put("reviewed",if(r.reviewed)1 else 0)
        }
        return writableDatabase.insert("sms_records",null,v)
    }
    fun updateReview(id:Long,label:MessageType) {
        writableDatabase.execSQL("UPDATE sms_records SET type=?,reviewed=1 WHERE id=?",arrayOf(label.name,id))
        val v=ContentValues().apply { put("sms_id",id); put("label",label.name); put("created_at",System.currentTimeMillis()) }
        writableDatabase.insert("feedback",null,v)
    }
    fun createTransaction(bank:String?,amount:Long?,sourceSmsId:Long):Long {
        val v=ContentValues().apply {
            put("bank",bank); if(amount==null) putNull("amount") else put("amount",amount)
            put("status",TransactionStatus.COMPLETED.name); put("created_at",System.currentTimeMillis()); put("source_sms_id",sourceSmsId)
        }
        return writableDatabase.insert("transactions",null,v)
    }
    fun countSms()=count("sms_records")
    fun countTransactions()=count("transactions")
    private fun count(table:String):Int=readableDatabase.rawQuery("SELECT COUNT(*) FROM $table",null).use{if(it.moveToFirst())it.getInt(0)else 0}
    fun latestSms():SmsRecord?=readableDatabase.rawQuery("SELECT id,sender,bank,type,amount,body,sanitized_body,confidence,created_at,reviewed FROM sms_records ORDER BY id DESC LIMIT 1",null).use{
        if(!it.moveToFirst()) return null
        SmsRecord(it.getLong(0),it.getString(1),it.getString(2),
            runCatching{MessageType.valueOf(it.getString(3))}.getOrDefault(MessageType.UNKNOWN),
            if(it.isNull(4))null else it.getLong(4),it.getString(5),it.getString(6),it.getFloat(7),it.getLong(8),it.getInt(9)==1)
    }
}
