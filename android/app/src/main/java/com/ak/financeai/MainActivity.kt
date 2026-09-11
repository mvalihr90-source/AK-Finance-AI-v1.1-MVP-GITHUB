package com.ak.financeai

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ak.financeai.data.FinanceDb
import com.ak.financeai.data.MessageType

class MainActivity:Activity(){
    private lateinit var db:FinanceDb
    private lateinit var status:TextView
    private lateinit var count:TextView
    private lateinit var pending:TextView
    private lateinit var meta:TextView
    private lateinit var transactions:TextView
    private lateinit var confirm:Button
    private lateinit var nonBank:Button
    private lateinit var correct:Button
    override fun onCreate(b:Bundle?){
        super.onCreate(b);setContentView(R.layout.activity_main);db=FinanceDb(this)
        status=findViewById(R.id.status);count=findViewById(R.id.count);pending=findViewById(R.id.pendingMessage);meta=findViewById(R.id.pendingMeta);transactions=findViewById(R.id.transactions)
        confirm=findViewById(R.id.confirmButton);nonBank=findViewById(R.id.nonBankButton);correct=findViewById(R.id.correctTransactionButton)
        findViewById<Button>(R.id.permissionButton).setOnClickListener{ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS),1001)}
        confirm.setOnClickListener{db.latestSms()?.let{db.updateReview(it.id,it.type);refresh()}}
        nonBank.setOnClickListener{db.latestSms()?.let{db.updateReview(it.id,MessageType.NON_BANK);refresh()}}
        correct.setOnClickListener{db.latestSms()?.let{db.updateReview(it.id,MessageType.BANK_TRANSACTION);if(it.bank!=null&&it.amount!=null)db.createTransaction(it.bank,it.amount,it.id);refresh()}}
        refresh()
    }
    override fun onResume(){super.onResume();refresh()}
    private fun refresh(){
        val ok=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED
        status.text=if(ok)"وضعیت پیامک: فعال" else "وضعیت پیامک: نیازمند دسترسی"
        count.text="پیام‌های ثبت‌شده: ${db.countSms()}"
        val r=db.latestSms()
        if(r==null){pending.text="هنوز پیامی دریافت نشده است.";meta.text="";setActions(false)}
        else{pending.text=r.sanitizedBody;meta.text="فرستنده: ${r.sender}\nبانک: ${r.bank?:"نامشخص"}\nنوع: ${label(r.type)}\nمبلغ: ${r.amount?:"نامشخص"}\nاطمینان: ${(r.confidence*100).toInt()}%";setActions(!r.reviewed)}
        transactions.text=if(db.countTransactions()==0)"هنوز تراکنش قطعی ثبت نشده است." else "تعداد تراکنش‌های قطعی: ${db.countTransactions()}"
    }
    private fun setActions(v:Boolean){confirm.visibility=if(v)View.VISIBLE else View.GONE;nonBank.visibility=if(v)View.VISIBLE else View.GONE;correct.visibility=if(v)View.VISIBLE else View.GONE}
    private fun label(t:MessageType)=when(t){MessageType.BANK_TRANSACTION->"تراکنش بانکی";MessageType.BANK_OTP->"رمز پویا / درخواست پرداخت";MessageType.BANK_ALERT->"هشدار بانکی";MessageType.NON_BANK->"غیر بانکی";MessageType.UNKNOWN->"نامشخص"}
}
