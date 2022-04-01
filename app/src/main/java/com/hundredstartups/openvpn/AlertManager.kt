package com.hundredstartups.openvpn

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import java.util.*

object AlertManager {

    interface OnTwoButtonDialogButtonClick {
        fun onPositiveClick()
        fun onNegativeClick()
    }

    fun showTwoButtonsDialog(
        activity: Activity?,
        textMessage: String,
        cancelable: Boolean,
        positiveText: String?,
        negativeText: String?,
        onTwoButtonDialogButtonClick: OnTwoButtonDialogButtonClick
    ) {
        if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.layout_dialog, null)
            dialogBuilder.setView(dialogView)
            val message = dialogView.findViewById<TextView>(R.id.tv_dialog)
            val actionPositive = dialogView.findViewById<TextView>(R.id.btn_dialog_positive)
            val actionNegative = dialogView.findViewById<TextView>(R.id.btn_dialog_negative)

            message.text = Html.fromHtml(textMessage.replace("\r".toRegex(), "\n"))
            if (!TextUtils.isEmpty(negativeText)) {
                actionNegative.visibility = View.VISIBLE
                actionNegative.text = negativeText
            }
            actionPositive.text = positiveText
            val alertDialog = dialogBuilder.create()
            Objects.requireNonNull(alertDialog.window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.setCancelable(cancelable)
            alertDialog.show()
            if (!TextUtils.isEmpty(negativeText)) {
                actionNegative.setOnClickListener { view: View? ->
                    alertDialog.dismiss()
                    onTwoButtonDialogButtonClick.onNegativeClick()
                }
            }
            actionPositive.setOnClickListener { view: View? ->
                alertDialog.dismiss()
                onTwoButtonDialogButtonClick.onPositiveClick()
            }
        }
    }
}