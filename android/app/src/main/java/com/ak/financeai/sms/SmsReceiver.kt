package com.ak.financeai.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.ak.financeai.data.FinanceDb
import com.ak.financeai.data.SmsRecord
import com.ak.financeai.ml.LocalAnalyzer

class SmsReceiver:BroadcastReceiver(){
    override fun onReceive(context:Context,intent:Intent){
        if(intent.action!=Telephony.Sms.Intents.SMS_RECEIVED_ACTION)return
        val msgs=Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if(msgs.isEmpty())return
        val sender=msgs.first().originatingAddress.orEmpty()
        val body=msgs.joinToString(""){it.messageBody.orEmpty()}
        if(body.isBlank())return
        val r=LocalAnalyzer().analyze(sender,body)
        val record=SmsRecord(sender=sender,bank=r.bank,type=r.type,amount=r.amount,body=body,sanitizedBody=r.sanitizedText,confidence=r.confidence,createdAt=System.currentTimeMillis())
        val db=FinanceDb(context.applicationContext)
        val id=db.insertSms(record)
        if(r.type.name=="BANK_TRANSACTION"&&r.confidence>=.90f&&r.bank!=null&&r.amount!=null)db.createTransaction(r.bank,r.amount,id)
    }
}
