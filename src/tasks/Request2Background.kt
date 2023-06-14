package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import kotlin.concurrent.thread

/**
 * 이 방식은 하나의 콜백 함수가 있다면 다음 콜백은 이전 콜백을 기다려야 한다.
 */
fun loadContributorsBackground(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    thread {
        val users = loadContributorsBlocking(service, req)
        updateResults(users)
    }
}