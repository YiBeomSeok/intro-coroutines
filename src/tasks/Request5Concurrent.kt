package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> =
    coroutineScope { // 마지막에 있는 값이 반환된다.
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

        // CoroutineScope에 소속
        repos.map { repo ->
            async {
                log("starting loading for ${repo.name}")
                delay(3000)
                service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            } // -> 이 자체는 List<User>
        } // `repos.map {}` 이므로 List<List<User>>
            .awaitAll()
            // 이 때 `flatten()`을 사용하면 List<User>로 풀어낼 수 있다.
            .flatten()
            .aggregate()
    }