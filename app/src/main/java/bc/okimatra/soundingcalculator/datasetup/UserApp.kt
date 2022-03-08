package bc.okimatra.soundingcalculator.datasetup

import android.app.Application

class UserApp:Application() {

    val db by lazy {
        UserDatabase.getInstance(this)
    }

}