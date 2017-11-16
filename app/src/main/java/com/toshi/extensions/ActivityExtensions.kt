package com.toshi.extensions

import android.content.Intent
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

fun AppCompatActivity.getColorById(@ColorRes id: Int) = ContextCompat.getColor(this, id)

inline fun <reified T> AppCompatActivity.startActivity() = startActivity(Intent(this, T::class.java))