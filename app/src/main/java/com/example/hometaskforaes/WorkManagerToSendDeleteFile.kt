package com.example.hometaskforaes

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class WorkManagerToSendDeleteFile(
    private val context: Context,
    workerParameters: WorkerParameters
) :
    Worker(context, workerParameters) {

    private val TAG = "WorkManagerToSendDelete"
    private lateinit var fireStorageReferences: StorageReference;


    override fun doWork(): Result {
        SharedStorage.initSharedPreferences(context);
        fireStorageReferences = FirebaseStorage.getInstance().reference.child("files");
        val listUriFiles = SharedStorage.getListUriFiles();

        for (i in listUriFiles.indices) {
            try {
                val uri = Uri.parse(listUriFiles[i].uri);
                sendAndDeleteFiles(uri);
            } catch (ex: Exception) {
                Log.e(TAG, "doWork: throws a NullPointer exception", ex);
            }
        }
        SharedStorage.initializingNewEmptyFiles();
        val outDataOutput= Data.Builder()
            .putString("status","finished").build();
        return Result.success(outDataOutput);
    }

    private fun sendAndDeleteFiles(uri: Uri) {
        val mimeType = context.contentResolver.getType(uri);
        val extension = mimeType!!.substring(mimeType.lastIndexOf('/') + 1);
        fireStorageReferences.child("${System.currentTimeMillis()}.$extension").putFile(uri);
        val row = context.contentResolver.delete(uri, null, null);
        Log.e(TAG, "sendAndDeleteFiles: $row")
    }


}