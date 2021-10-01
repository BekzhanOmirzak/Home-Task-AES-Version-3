package com.example.hometaskforaes

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedStorage {

    private lateinit var sharedPreferences: SharedPreferences;
    private val turnsType = object : TypeToken<MutableList<UriString>>() {}.type
    private const val TAG = "SharedStorage"
    private const val MY_SHARED_PREF_NAME = "com.example.hometaskforaes";

    fun initSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(MY_SHARED_PREF_NAME, Context.MODE_PRIVATE);
        val oldFilesString = sharedPreferences.getString("files", null);
        if (oldFilesString == null)
            initializingNewEmptyFiles();

    }

    fun initializingNewEmptyFiles() {
        val uriFiles = mutableListOf<UriString>();
        val uriFile_str = Gson().toJson(uriFiles);
        sharedPreferences.edit().putString("files", uriFile_str).commit();
    }

    fun saveListUriFiles(uri: Uri) {
        val oldUriFilesString = sharedPreferences.getString("files", null);
        if (oldUriFilesString == null)
            initializingNewEmptyFiles();
        val oldUriFilesList = Gson().fromJson<MutableList<UriString>>(oldUriFilesString, turnsType);
        oldUriFilesList.add(UriString(uri.toString()));
        initializingNewEmptyFiles();                                         //I need this immediately
        val newUriFilesString = Gson().toJson(oldUriFilesList);
        sharedPreferences.edit().putString("files", newUriFilesString).apply();
    }


    fun getListUriFiles(): List<UriString> {
        val uriFilesString = sharedPreferences.getString("files", "");
        Log.e(TAG, "getListUriFiles: Bekzhan uri str  $uriFilesString")
        return Gson().fromJson<MutableList<UriString>>(uriFilesString, turnsType);
    }


}