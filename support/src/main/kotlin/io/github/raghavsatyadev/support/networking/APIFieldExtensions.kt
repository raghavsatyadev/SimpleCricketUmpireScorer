package io.github.raghavsatyadev.support.networking

import android.text.TextUtils
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.Constants.Other.MEDIA_TYPE_JSON
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.FileExtensions.getMimeType
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder

@Suppress("unused", "MemberVisibilityCanBePrivate")
object APIFieldExtensions {

    /**
     * Converts File into Multipart Object
     *
     * @param fileParameter parameter name which goes into APIs
     * @return [MultipartBody.Part]
     */
    fun String.toMultiPartFile(fileParameter: String): MultipartBody.Part? {
        if (!TextUtils.isEmpty(this)) {
            val file = File(this)
            try {
                return file.toMultiPartFile(fileParameter)
            } catch (e: Exception) {
                AppLog.loge(false, "APIFieldUtil.java", "getMultiPartFile", e, Exception())
            }
        }
        return null
    }

    /**
     * Converts File into Multipart Object
     *
     * @param fileParameter parameter name which goes into APIs
     * @return [MultipartBody.Part]
     */
    fun File.toMultiPartFile(fileParameter: String): MultipartBody.Part? {
        try {
            val type = getMimeType()?.toMediaTypeOrNull()
            return MultipartBody.Part.createFormData(fileParameter, name, asRequestBody(type))
        } catch (e: Exception) {
            AppLog.loge(false, kotlinFileName, "getMultiPartFile", e, Exception())
        }
        return null
    }

    fun File.toFileRequestBody(): RequestBody {
        val type = getMimeType()?.toMediaTypeOrNull()
        return asRequestBody(type)
    }

    fun String.toFileRequestBody(): RequestBody {
        return File(this).toFileRequestBody()
    }

    /** @return RequestBody of given String */
    fun String.toMultipartString(): RequestBody {
        return this.toRequestBody(MultipartBody.FORM)
    }

    fun HashMap<String, String?>?.removeNullValues(): HashMap<String, String> {
        val map = HashMap<String, String>()
        this?.let { it1 ->
            it1.entries.parallelStream().forEach { map[it.key] = it.value ?: "" }
        }
        return map
    }

    fun HashMap<String, String>.convertHashMapToURLParameters(): String {
        var parameters = ""
        this.let { map ->
            map.entries.parallelStream().forEach {
                parameters += it.key + "=" + URLEncoder.encode(
                    it.value,
                    Charsets.UTF_8.displayName()
                ) + "&"
            }
        }
        return parameters
    }

    fun String.toJSONRequestBody(): RequestBody {
        val mediaType = MEDIA_TYPE_JSON.toMediaType()
        return this.toRequestBody(mediaType)
    }

    fun Any.toJSONRequestBody(): RequestBody {
        val string = toJsonString()
        val mediaType = MEDIA_TYPE_JSON.toMediaType()
        return string.toRequestBody(mediaType)
    }

    fun JSONObject.toJSONRequestBody(): RequestBody {
        val mediaType = MEDIA_TYPE_JSON.toMediaType()
        return this.toString().toRequestBody(mediaType)
    }

    fun JSONArray.toJSONRequestBody(): RequestBody {
        val mediaType = MEDIA_TYPE_JSON.toMediaType()
        return this.toString().toRequestBody(mediaType)
    }
}

