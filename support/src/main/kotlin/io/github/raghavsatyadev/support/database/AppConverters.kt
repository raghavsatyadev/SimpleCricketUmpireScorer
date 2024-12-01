package io.github.raghavsatyadev.support.database

import androidx.room.TypeConverter
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.match_record.Team

class AppConverters {
    @TypeConverter
    fun fromTeam(team: Team): String {
        return team.toJsonString()
    }

    @TypeConverter
    fun toTeam(teamString: String): Team {
        return teamString.toKotlinObject()
    }
}
