package com.example.tp5

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import java.io.File

object ImageUtils {

    fun loadPhoto(context: Context, uriString: String?, imageView: ImageView) {
        if (uriString.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_person_placeholder)
            return
        }

        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" || uri.scheme == null) {
                val filePath = uri.path ?: uriString
                val file = File(filePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                        return
                    }
                }
            }

            // Fallback to ContentResolver / setImageURI
            imageView.setImageURI(null)
            imageView.setImageURI(uri)
        } catch (_: Exception) {
            imageView.setImageResource(R.drawable.ic_person_placeholder)
        }
    }
}
