package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    return repos.map { repo ->
        // 독립적인 코드 // 그래서 부모를 cancel 해도 취소되지 않는다. // 구조화된 동시성 안에 포함되지 않았다.
        // GlobalScope는 누구의 자식도 아님
        GlobalScope.async {
            log("starting loading for ${repo.name}")
            delay(3000)
            service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }.awaitAll().flatten().aggregate()
}