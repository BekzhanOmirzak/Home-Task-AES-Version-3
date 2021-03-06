package com.example.hometaskforaes

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.work.*
import com.example.hometaskforaes.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {


    private final val TAG = javaClass.name.toString();
    private lateinit var key: SecretKey;
    private lateinit var binding: ActivityMainBinding;
    private lateinit var fireStorageReferences: StorageReference;
    private lateinit var listFiles: MutableList<Uri>;
    private var numberFiles = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)
        fireStorageReferences = FirebaseStorage.getInstance().reference.child("files");
        val keyGenerator = KeyGenerator.getInstance("AES");
        listFiles = mutableListOf();
        keyGenerator.init(128);
        key = keyGenerator.generateKey();
        Utils.permissionToReadAndCreateFile(this);
        SharedStorage.initSharedPreferences(this);


        binding.btnEnrypt.setOnClickListener {

            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.type = "*/*";
            startActivityForResult(intent, 1);

        }

        binding.btnDecrypt.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.type = "*/*";
            startActivityForResult(intent, 2);
        }

        binding.btnSend.setOnClickListener {
            if (listFiles.isNotEmpty())
                sendAndDeleteFiles();
            else
                Toast.makeText(this, "???????? ???? ???? ?????????????????????? ?????????????? ??????????", Toast.LENGTH_SHORT)
                    .show()
        }

        launchingPeriodicWorkManager();


    }


    private fun launchingPeriodicWorkManager() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            WorkManagerToSendDeleteFile::class.java, 30,
            TimeUnit.MINUTES
        ).setConstraints(constraint)
            .build();
        val workManager = WorkManager.getInstance(this);
        workManager.enqueue(periodicWorkRequest);

        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id).observe(this) {
            if (it.state.isFinished) {
                if (it.outputData.getString("state").equals("finished")) {
                    numberFiles=-1;
                    increaseNumberOfFiles();
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Utils.permissionToReadAndCreateFile(this);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {

            val file = Utils.getFileFromGetContentUri(contentResolver, data.data!!);
            val mimeType = contentResolver.getType(data.data!!);
            val extension = mimeType!!.substring(mimeType.lastIndexOf('/') + 1);
            binding.progressBar.visibility = View.VISIBLE;
            CoroutineScope(Dispatchers.IO).launch {
                val new_file = getTempEmptyFileForEncryptDecrypt(extension, "Encrypted files");
                val result = AESEncryptionAndDecryption.encrypt(key, file!!, new_file!!);
                withContext(Dispatchers.Main) {
                    val result_message = if (result) "??????????????..." else "??????????????????..."
                    Toast.makeText(this@MainActivity, result_message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    if (result) binding.txtEncryptedPath.visibility = View.VISIBLE;
                    binding.txtEncryptedPath.text =
                        "Path to Encrypted file is DCIM/Encrypted files";
                }
            }

        }

        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            Log.e(TAG, "onActivityResult: normal uri that can be deleted  ${data.data}")
            val file = Utils.getFileFromGetContentUri(contentResolver, data.data!!);
            val mimeType = contentResolver.getType(data.data!!);
            val extension = mimeType!!.substring(mimeType.lastIndexOf('/') + 1);
            binding.progressBar.visibility = View.VISIBLE;
            CoroutineScope(Dispatchers.IO).launch {
                val new_file = getTempEmptyFileForEncryptDecrypt(extension, "Decrypted files");
                val result = AESEncryptionAndDecryption.decrypt(key, file!!, new_file!!);
                updateNumberFiles(new_file);
                withContext(Dispatchers.Main) {
                    val result_message = if (result) "??????????????..." else "??????????????????..."
                    Toast.makeText(this@MainActivity, result_message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    if (result) binding.txtDecryptedPath.visibility = View.VISIBLE;
                    binding.txtDecryptedPath.text =
                        "Path to Decrypted files is : DCIM/Decrypted files";
                    increaseNumberOfFiles();
                }

            }

        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    private fun updateNumberFiles(new_file: File?) {

        if (new_file != null) MediaScannerConnection.scanFile(
            this@MainActivity,
            arrayOf(new_file.absolutePath),
            null
        ) { path, uri ->
            listFiles.add(uri);
            SharedStorage.saveListUriFiles(uri);
        }

    }


    private fun sendAndDeleteFiles() {
        binding.progressBar.visibility = View.VISIBLE;
        listFiles.stream().forEach { uri ->
            CoroutineScope(Dispatchers.IO).launch {
                val mimeType = contentResolver.getType(uri);
                val extension = mimeType!!.substring(mimeType.lastIndexOf('/') + 1);
                fireStorageReferences.child("${System.currentTimeMillis()}.$extension")
                    .putFile(uri);
                val row = contentResolver.delete(uri, null, null);
                Log.e(TAG, "sendAndDeleteFiles: $row")
            }
        }
        Toast.makeText(this, "?????????? ?????????????? ???????????????????? ?? ??????????????", Toast.LENGTH_SHORT).show()
        listFiles.removeAll(listFiles);
        binding.progressBar.visibility = View.GONE;
        numberFiles = -1;
        increaseNumberOfFiles();
        CoroutineScope(Dispatchers.IO).launch {
            SharedStorage.initializingNewEmptyFiles();
        }

    }


    private fun getTempEmptyFileForEncryptDecrypt(extension: String, fileName: String): File? {

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            fileName
        )

        if (!file.exists())
            if (!file.mkdirs()) {
                Log.e(TAG, "getFileFromUri: Failed to create file Directory")
                return null;
            }

        val simpleDataFormat = SimpleDateFormat("HH:mm:ss")

        return File(file.absolutePath + File.separator + simpleDataFormat.format(Date()) + ".$extension");

    }

    private fun increaseNumberOfFiles() {
        numberFiles++;
        binding.txtNumFiles.text = "???????????????????? ?????????? : $numberFiles"
    }


}

