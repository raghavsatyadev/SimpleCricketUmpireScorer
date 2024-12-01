package io.github.raghavsatyadev.support

import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import java.io.File
import java.io.IOException

@Suppress("unused", "MemberVisibilityCanBePrivate")
object StorageUtils {
    fun getAppStorageDir(): File {
        return CoreApp.instance.filesDir
    }

    fun getCacheDirectory(): File {
        return CoreApp.instance.cacheDir
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
            AppLog.loge(
                false,
                kotlinFileName,
                "createFile",
                e,
                Exception()
            )
        }
        return file
    }

    /**
     * Create a file in the storage location taken from the
     * [StorageUtils.getAppStorageDir]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getFileWithoutCreating(
        fileName: String,
        parentFolderName: String? = null,
        deleteIfExists: Boolean = false,
    ): File {
        val rootDir = if (parentFolderName.isNullOrEmpty()) {
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
        } else
            rootDir
    }
}