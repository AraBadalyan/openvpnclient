package com.hundredstartups.openvpn.utils

import org.joda.time.Period
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object FormatterUtils {

    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    fun formatDate(calendar: Calendar): String {
        return dateFormat.format(calendar.time)
    }

    fun formatHttpDateParam(calendar: Calendar): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return sdf.format(calendar.time)
    }

    fun formatLongToDate(miliseconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = miliseconds
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
        return sdf.format(calendar.time)
    }

//    fun formatHttpDateParam(dateTime: String?): String? {
//        val dateFrom = SimpleDateFormat("MM/dd/yy")
//        val d1: Date
//        d1 = try {
//            dateFrom.parse(dateTime)
//        } catch (e: ParseException) {
//            e.printStackTrace()
//            return null
//        }
//        val dateTo = SimpleDateFormat("dd/MM/yy", Locale.US)
//        return dateTo.format(d1)
//    }

    fun formatDateSignUp(calendar: Calendar): String {
        val sdf = SimpleDateFormat("dd / MM / yyyy", Locale.US)
        return sdf.format(calendar.time)
    }

    fun formatDateSignUp(dateTime: String?): String? {
        val dateFrom = SimpleDateFormat("MM/dd/yy")
        val d1: Date = try {
            dateFrom.parse(dateTime)
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }
        val dateTo = SimpleDateFormat("dd / MM/ yyyy", Locale.US)
        return dateTo.format(d1)
    }

    fun formatValueTwoSymbolsDot(pValue: Double?): String {
        return DecimalFormat("0.00", DecimalFormatSymbols(Locale.getDefault())).format(pValue)
    }

    fun formatValueTwoSymbolsDot(pValue: String?): String {
        return DecimalFormat(
            "0.00",
            DecimalFormatSymbols(Locale.getDefault())
        ).format(java.lang.Double.valueOf(pValue))
    }

    fun formatMoney(s: String?, currency: String?): String {
        return String.format(Locale.US, "$currency%s", s)
    }

    fun formatMoney(s: Double?, currency: String?): String {
        return String.format(Locale.US, "$currency%s", formatValueTwoSymbolsDot(s))
    }

    fun convertToDate(text: String?): Date {
        var convertedDate = Date()
        try {
            convertedDate = dateFormat.parse(text)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return convertedDate
    }

    fun calculateExpiresIn(endDate: Long): String {
        var text = ""
        val period = Period(System.currentTimeMillis(), endDate)
        if (period.years > 0) {
            text += "${period.years}y. "
        }
        if (period.months > 0) {
            text += "${period.months}m. "
        }
        if (period.weeks > 0 && (period.years <= 0 || period.months <= 0)) {
            text += "${period.weeks}w. "
        }
        if (period.days > 0 && (period.months <= 0 || period.weeks <= 0)) {
            text += "${period.days}d. "
        }
        if (period.hours > 0 && (period.weeks <= 0 || period.days <= 0)) {
            text += "${period.hours}h. "
        }
        if (period.minutes > 0 && (period.days <= 0 || period.hours <= 0)) {
            text += "${period.minutes}min."
        }
        return text
    }

    fun calculateDayFromTo(endDate: Long): Int {
        val today = Calendar.getInstance()
        today[Calendar.HOUR_OF_DAY] = 0
        val selected = Calendar.getInstance()
        try {
//            selected.time = dateFormat.parse(selectedDate)
            selected.time = Date(endDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return daysBetween(today, selected)
    }

    fun daysBetween(day1: Calendar, day2: Calendar): Int {
        var dayOne = day1.clone() as Calendar
        var dayTwo = day2.clone() as Calendar
        return if (dayOne[Calendar.YEAR] == dayTwo[Calendar.YEAR]) {
            Math.abs(dayOne[Calendar.DAY_OF_YEAR] - dayTwo[Calendar.DAY_OF_YEAR])
        } else {
            if (dayTwo[Calendar.YEAR] > dayOne[Calendar.YEAR]) {
                //swap them
                val temp = dayOne
                dayOne = dayTwo
                dayTwo = temp
            }
            var extraDays = 0
            val dayOneOriginalYearDays = dayOne[Calendar.DAY_OF_YEAR]
            while (dayOne[Calendar.YEAR] > dayTwo[Calendar.YEAR]) {
                dayOne.add(Calendar.YEAR, -1)
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR)
            }
            extraDays - dayTwo[Calendar.DAY_OF_YEAR] + dayOneOriginalYearDays
        }
    }

    fun formatWithoutDot(pValue: Double): String {
        return DecimalFormat("0", DecimalFormatSymbols(Locale.getDefault())).format(pValue)
    }

    fun getDateFormat(date: String?): Date {
        try {
            return SimpleDateFormat("dd/MM/yy hh:mm").parse(date)
        } catch (pE: ParseException) {
            pE.printStackTrace()
        }
        return Date()
    }
}
