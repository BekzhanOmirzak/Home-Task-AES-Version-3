package com.example.hometaskforaes

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.hometaskforaes.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {


    private final val TAG = javaClass.name.toString();
    private lateinit var key: SecretKey;
    private lateinit var binding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        val keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        key = keyGenerator.generateKey();

        binding.btnEnrypt.setOnClickListener {

            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.type = "*/*";
            startActivityForResult(intent, 1);

        }

        binding.btnDecrypt.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(
                Uri.parse(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path
                            + File.separator + "Encrypted files" + File.separator
                ), "*/*"
            );
            startActivityForResult(intent, 2);

        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Utils.permissionToCreateFile(this);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {

            val file = Utils.getFileFromGetContentUri(contentResolver, data.data!!);
            val mimeType = contentResolver.getType(data.data!!);
            val extension = mimeType!!.substring(mimeType.lastIndexOf('/') + 1);
            val new_file = getTempEmptyFileForEncryptDecrypt(extension, "Encrypted files");
            AESEncryptionAndDecryption.encrypt(key, file!!, new_file!!);
            binding.txtEncryptedPath.visibility = View.VISIBLE;
            binding.txtEncryptedPath.text = "Path to Encrypted file is DCIM/Encrypted files";
            progressSimulation();
        }

        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {

            val file = Utils.getFileFromGetContentUri(contentResolver, data.data!!);
            val mimeType = contentResolver.getType(data.data!!);
            val extension = mimeType!!.substring(mimeType.lastIndexOf('/') + 1);
            val new_file = getTempEmptyFileForEncryptDecrypt(extension, "Decrypted files");
            AESEncryptionAndDecryption.decrypt(key, file!!, new_file!!);
            binding.txtDecryptedPath.visibility = View.VISIBLE;
            binding.txtDecryptedPath.text = "Path to Decrypted files is : DCIM/Decrypted files";
            progressSimulation();

        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private fun progressSimulation() {
        binding.progressBar.visibility = View.VISIBLE;

        CoroutineScope(Dispatchers.IO).launch {
            delay(2000);
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE;
            }
        }

    }


}