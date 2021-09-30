package com.example.hometaskforaes

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.nfc.Tag
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.File
import java.lang.StringBuilder
import javax.crypto.Cipher

object Utils {

    private val TAG = javaClass.name.toString();

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun permissionToReadAndCreateFile(activity: Activity) {
        val permission = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                1
            );
        }
    }

    fun getPathFromActionPickUp(uri: Uri, activity: Activity): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor =
            activity.contentResolver.query(uri, projection, null, null, null)
                ?: return null
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s: String = cursor.getString(column_index)
        cursor.close()
        return s
    }

    fun getFileFromGetContentUri(contentResolver: ContentResolver, uri: Uri): File? {

        val myDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Kotlin  App"
        );

        if (!myDirectory.exists())
            if (!myDirectory.mkdirs()) {
                Log.e(TAG, "getFileFromUri: Failed to create file Directory")
                return null;
            }

        val file = File.createTempFile("suffix", "prefix", myDirectory);
        file.outputStream().use {
            contentResolver.openInputStream(uri)?.copyTo(it);
            it.close();
        }
        return file;

    }


}





//if (requestCode == 3 && resultCode == RESULT_OK && null != data) {
//
//            if (null != data.clipData) {
//                val listFiles = mutableListOf<Uri>();
//                for (i in 0 until data.clipData!!.itemCount) {
//                    val uri = data.clipData!!.getItemAt(i).uri;
//                    uri?.let {
//                        listFiles.add(uri);
//                    }
//                }
//                sendAndDeleteFiles(listFiles);
//            } else if (null != data.data) {
//                val uri = data.data;
//                uri?.let {
//                    sendAndDeleteFiles(
//                        listOf(uri)
//                    );
//                }
//            }
//        }


//val selectedUri =
//    Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + File.separator + "Decrypted files" + File.separator)
//Log.e(TAG, "onCreate: $selectedUri")
//val intent = Intent(Intent.ACTION_GET_CONTENT);
//intent.type = "*/*";
//intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//startActivityForResult(intent, 3);

