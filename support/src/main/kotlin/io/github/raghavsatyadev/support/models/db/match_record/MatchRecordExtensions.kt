package io.github.raghavsatyadev.support.models.db.match_record

import io.github.raghavsatyadev.support.extensions.DateExtensions.formatMillisToDate
import io.github.raghavsatyadev.support.models.BasicMatchUIDetails
import java.util.Locale

object MatchRecordExtensions {
    fun MatchRecord.getWickets(shouldPrepareTeam1Details: Boolean) =
        if (shouldPrepareTeam1Details) {
            team1Detail.wickets
        } else {
            team2Detail.wickets
        }

    fun MatchRecord.getCRR(shouldPrepareTeam1Details: Boolean): String {
        val runs = if (shouldPrepareTeam1Details) {
            team1Detail.runs
        } else {
            team2Detail.runs
        }
        val balls = if (shouldPrepareTeam1Details) {
            team1Detail.balls
        } else {
            team2Detail.balls
        }
        return if (balls == 0) {
            "N/A"
        } else {
            val crr = runs * 6.0 / balls
            String.format(
                Locale.getDefault(),
                "%.2f",
                crr
            )
        }
    }

    fun MatchRecord.getRRR(): String {
        val shouldPrepareTeam1Details = isTeam1CurrentlyBatting()

        val currentRuns = getRuns(shouldPrepareTeam1Details)
        val currentBalls = getBalls(shouldPrepareTeam1Details)
        val otherTeamRuns = getRuns(!shouldPrepareTeam1Details)

        return getRRR(
            currentRuns,
            otherTeamRuns,
            currentBalls,
        )
    }

    /**
     * Calculates the Required Run Rate (RRR) in a cricket match.
     *
     * @param currentBattingTeamRuns The runs scored by the current batting
     *    team.
     * @param otherTeamRuns The total runs scored by the other team.
     * @param currentBattingTeamBalls The balls faced by the current batting
     *    team.
     * @return The Required Run Rate (RRR) as a formatted String.
     */
    fun MatchRecord.getRRR(
        currentBattingTeamRuns: Int,
        otherTeamRuns: Int,
        currentBattingTeamBalls: Int,
    ): String {
        val ballsRemaining = ballsPerInning - currentBattingTeamBalls
        val runsRequired = otherTeamRuns + 1 - currentBattingTeamRuns

        // Check if the target has been achieved or if there are no balls left
        if (ballsRemaining <= 0 || runsRequired <= 0) {
            return if (runsRequired <= 0) {
                "0.0"
            } else {
                "N/A"
            }
        }

        // Calculate Required Run Rate (RRR)
        val rrr = (runsRequired.toDouble() * 6) / ballsRemaining

        // Format the result to 2 decimal places
        return String.format(
            Locale.getDefault(),
            "%.2f",
            rrr
        )
    }

    /**
     * Get required runs balls
     *
     * @param currentBattingTeamRuns
     * @param otherTeamRuns
     * @param currentBattingTeamBalls
     * @return required run and balls in format of Runs (Balls)
     */
    fun MatchRecord.getRequiredRunsBalls(
        currentBattingTeamRuns: Int,
        otherTeamRuns: Int,
        currentBattingTeamBalls: Int,
    ): String {
        val runsRequired = otherTeamRuns + 1 - currentBattingTeamRuns
        val ballsRemaining = ballsPerInning - currentBattingTeamBalls

        return "$runsRequired ($ballsRemaining)"
    }

    fun MatchRecord.toBasicMatchUIDetails(needTeam1Details: Boolean? = null): BasicMatchUIDetails {
        val shouldPrepareTeam1Details = needTeam1Details ?: isTeam1CurrentlyBatting()

        val currentRuns = getRuns(shouldPrepareTeam1Details)
        val currentWickets = getWickets(shouldPrepareTeam1Details)
        val currentBalls = getBalls(shouldPrepareTeam1Details)
        val currentRunsAndWickets = formatCurrentRunsAndWickets(
            currentRuns,
            currentWickets
        )
        val currentOvers = "${formatToOvers(currentBalls)} / ${formatToOvers(ballsPerInning)}"
        val currentCRR = getCRR(shouldPrepareTeam1Details)
        val otherTeamRuns = getRuns(!shouldPrepareTeam1Details)
        val currentRRR = getRRR(
            currentRuns,
            otherTeamRuns,
            currentBalls,
        )
        val requiredRunsBalls = getRequiredRunsBalls(
            currentRuns,
            otherTeamRuns,
            currentBalls,
        )

        return BasicMatchUIDetails(
            currentTeamName = if (shouldPrepareTeam1Details) {
                team1Detail.teamName
            } else {
                team2Detail.teamName
            },
            currentRunsAndWickets = currentRunsAndWickets,
            currentOvers = currentOvers,
            currentCRR = currentCRR,
            isFirstInningComplete = isFirstInningComplete,
            currentRRR = currentRRR,
            requiredRunsBalls = requiredRunsBalls,
            matchStatus = status,
        )
    }

    fun MatchRecord.getRuns(shouldPrepareTeam1Details: Boolean) = if (shouldPrepareTeam1Details) {
        team1Detail.runs
    } else {
        team2Detail.runs
    }

    fun MatchRecord.getTeam1FormattedScore() =
        "${getRuns(true)}-${getWickets(true)} (${formatToOvers(getBalls(true))})"

    fun MatchRecord.getTeam2FormattedScore() =
        "${getRuns(false)}-${getWickets(false)} (${formatToOvers(getBalls(false))})"

    fun formatCurrentRunsAndWickets(
        runs: Int,
        wickets: Int,
    ) = "$runs-$wickets"

    fun formatToOvers(balls: Int): String {
        return "${balls / 6}.${balls % 6}"
    }

    fun MatchRecord.getBalls(shouldPrepareTeam1Details: Boolean) = if (shouldPrepareTeam1Details) {
        team1Detail.balls
    } else {
        team2Detail.balls
    }

    /**
     * Takes into account following things
     * - [MatchRecord.isTeam1BattingFirst] if team 1 took first batting or not
     * - [MatchRecord.isFirstInningComplete] if first inning is complete or not
     */
    fun MatchRecord.isTeam1CurrentlyBatting() = when {
        isTeam1BattingFirst && !isFirstInningComplete -> true
        !isTeam1BattingFirst && !isFirstInningComplete -> false
        isTeam1BattingFirst -> false
        else -> true
    }

    fun MatchRecord.isMatchCompleted() =
        !(status == MatchStatus.NOT_STARTED || status == MatchStatus.IN_PROGRESS)

    fun BasicMatchUIDetails.isMatchCompleted() =
        !(matchStatus == MatchStatus.NOT_STARTED || matchStatus == MatchStatus.IN_PROGRESS)

    fun MatchRecord.getMatchTimings(): String {
        val matchEndTimeString = if (isMatchCompleted()) {
            " - ${endDateTime.formatMillisToDate()}"
        } else {
            ""
        }
        val matchStartTime = startDateTime.formatMillisToDate()
        val matchTimings = matchStartTime + matchEndTimeString
        return matchTimings
    }
}