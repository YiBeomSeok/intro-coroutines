package tasks

import contributors.*

// Concurrent 방식이 아니라 하나씩 순차대로 하는 방식이기 때문에 느려짐
suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    val allUsers = mutableListOf<User>()

    for ((index, repo) in repos.withIndex()) {
        val users = service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
        // users = 개별 레포지토리의 기여자. // 전체 레포지토리의 기여자를 알고 싶음.
        allUsers += users
        allUsers.aggregate()
        val isCompleted = index == repos.lastIndex
        updateResults(allUsers, isCompleted)
    }
}
