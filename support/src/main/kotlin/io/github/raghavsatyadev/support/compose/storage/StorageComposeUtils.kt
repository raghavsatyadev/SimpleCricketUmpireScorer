package io.github.raghavsatyadev.support.compose.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import kotlinx.io.IOException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageComposeUtils
@Inject
constructor(@param:ApplicationContext private val context: Context) {
  fun getAppStorageDir(): File {
    return context.filesDir
  }

  fun getCacheDirectory(): File {
    return context.cacheDir
  }

  fun createFile(
    parentFolderName: String? = null,
    fileName: String,
    replace: Boolean = false,
  ): File {
    val file = getFileWithoutCreating(fileName, parentFolderName, replace)
    try {
      if (replace || !file.exists()) file.createNewFile()
    } catch (e: IOException) {
      AppLog.loge(false, kotlinFileName, "createFile", e, Exception())
    }
    return file
  }

  /** Create a file in the storage location taken from the [StorageComposeUtils.getAppStorageDir] */
  fun getFileWithoutCreating(
    fileName: String,
    parentFolderName: String? = null,
    deleteIfExists: Boolean = false,
  ): File {
    val rootDir =
      if (parentFolderName.isNullOrEmpty()) {
        getAppStorageDir()
      } else {
        File(getAppStorageDir(), parentFolderName)
      }

    rootDir.mkdirs()

    return if (fileName.isNotEmpty()) {
      val file = File(rootDir, fileName)
      if (file.exists() && deleteIfExists) {
        file.delete()
      }
      file
    } else rootDir
  }
}
