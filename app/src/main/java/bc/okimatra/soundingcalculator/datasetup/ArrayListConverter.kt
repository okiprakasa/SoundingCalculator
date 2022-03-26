package bc.okimatra.soundingcalculator.datasetup

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object ArrayListConverter {
    @TypeConverter
    fun fromString(value: String?): ArrayList<String> {
        val listType: Type = object : TypeToken<ArrayList<String?>?>(){}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromArrayListOfDouble(list: ArrayList<Double>?): String {
        return list?.joinToString(separator = ";") { it.toString() } ?: ""
    }

    @TypeConverter
    fun toArrayListOfDouble(string: String?): ArrayList<Double> {
        return ArrayList(string?.split(";")?.mapNotNull { it.toDoubleOrNull() } ?: emptyList())
    }
}