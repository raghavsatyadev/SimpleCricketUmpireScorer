package io.github.raghavsatyadev.support

object Constants {
    object Other {
        const val SPLASH_COUNTER: Long = 3000

        const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
    }

    object DB {
        const val NAME: String = "SCUC"
        const val VERSION = 1

        object Tables {
            const val MATCH_RECORD_TABLE = "match_record"

            const val MATCH_RECORD_ID = "id"
        }
    }

    object NotificationKeys {
        const val MAIN_KEY = "main"
    }
}