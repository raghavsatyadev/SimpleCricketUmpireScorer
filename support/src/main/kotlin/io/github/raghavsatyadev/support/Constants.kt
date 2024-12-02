package io.github.raghavsatyadev.support

object Constants {
    object Other {
        const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
    }

    object DB {
        const val NAME: String = "SCUC"
        const val VERSION = 1

        object Tables {
            const val MATCH_RECORD_TABLE = "match_record"
        }
    }

    object FieldKeys {
        const val MATCH_RECORD_ID = "id"

        const val USER_ID = "userID"
    }

    object FirebaseConstants {
        object Collections {
            const val USER = "User"
        }
    }

    object NotificationKeys {
        const val MAIN_KEY = "main"
    }
}