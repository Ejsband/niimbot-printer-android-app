package ru.odinesina.niimbot

import android.app.Application

class NiibotApplication : Application() {

    private var niibot: NiibotApplication? = null

    override fun onCreate() {
        super.onCreate()
        niibot = this
    }

    fun getNiibotApplicationInstance(): NiibotApplication? {
        return niibot
    }
}