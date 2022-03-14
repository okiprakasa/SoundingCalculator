package bc.okimatra.soundingcalculator.datasetup

import androidx.room.TypeConverter
import java.util.*

object DateTypeConverter {
    @TypeConverter
    fun calendarFromTimestamp(value: String?): Calendar? {
        if (value == null) {
            return null
        }
        val cal: Calendar = GregorianCalendar()
        cal.timeInMillis = value.toLong() * 1000
        return cal
    }

    @TypeConverter
    fun dateToTimestamp(cal: Calendar?): String? {
        return if (cal == null) {
            null
        } else "" + cal.timeInMillis / 1000
    }
}
