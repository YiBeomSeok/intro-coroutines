package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope { // 마지막에 있는 값이 반환된다.
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

        val allUsers = mutableListOf<User>()
        val channel = Channel<List<User>>()

        for (repo in repos) {
            launch {
                val users = service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }

        repeat(repos.size) { index ->
            val users = channel.receive()
            allUsers += users
            val isCompleted = index == repos.lastIndex
            updateResults(allUsers.aggregate(), isCompleted)
        }
    }
}
