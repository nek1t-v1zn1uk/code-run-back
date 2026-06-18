package com.example.coderun.config

import com.example.coderun.domain.contest.entity.Contest
import com.example.coderun.domain.contest.entity.ContestMember
import com.example.coderun.domain.contest.entity.ContestProblem
import com.example.coderun.domain.contest.repository.ContestMemberRepository
import com.example.coderun.domain.contest.repository.ContestProblemRepository
import com.example.coderun.domain.contest.repository.ContestRepository
import com.example.coderun.domain.problems.entity.EvaluationType
import com.example.coderun.domain.problems.entity.Problem
import com.example.coderun.domain.problems.entity.ProblemDifficulty
import com.example.coderun.domain.problems.entity.ProblemTopic
import com.example.coderun.domain.problems.repository.ProblemRepository
import com.example.coderun.domain.problems.repository.ProblemTopicRepository
import com.example.coderun.domain.solutions.repository.AvailableLanguageRepository
import com.example.coderun.domain.tests.entity.ScriptChecker
import com.example.coderun.domain.tests.entity.Test
import com.example.coderun.domain.tests.repository.ScriptCheckerRepository
import com.example.coderun.domain.tests.repository.TestRepository
import com.example.coderun.domain.users.User
import com.example.coderun.domain.users.UserRepository
import com.example.coderun.domain.users.UserRoles
import com.example.coderun.domain.solutions.entity.Solution
import com.example.coderun.domain.solutions.entity.SolutionStatus
import com.example.coderun.domain.solutions.repository.SolutionRepository
import com.example.coderun.domain.comments.entity.Comment
import com.example.coderun.domain.comments.repository.CommentRepository
import jakarta.persistence.EntityManager
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Random

@Component
@Profile("demo")
class DemoDataInitializer(
    private val userRepository: UserRepository,
    private val problemRepository: ProblemRepository,
    private val problemTopicRepository: ProblemTopicRepository,
    private val contestRepository: ContestRepository,
    private val contestMemberRepository: ContestMemberRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val availableLanguageRepository: AvailableLanguageRepository,
    private val testRepository: TestRepository,
    private val scriptCheckerRepository: ScriptCheckerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val entityManager: EntityManager,
    private val solutionRepository: SolutionRepository,
    private val commentRepository: CommentRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        println("=== Starting Demo Data Initialization (Truncate & Seed) ===")

        // 1. Truncate tables (cascade to avoid foreign key violations, restart identity to reset IDs to 1)
        entityManager.createNativeQuery(
            "TRUNCATE TABLE comments, solutions, contest_members, contest_problems, contests, tests, problems, script_checkers, users, problem_topics RESTART IDENTITY CASCADE"
        ).executeUpdate()
        println("Database truncated successfully (excluding available_languages).")

        // 2. Fetch languages
        val pythonLang = availableLanguageRepository.findByLanguage("python")
            ?: throw IllegalStateException("Python language not found in DB")

        // 3. Seed Users
        val admin = userRepository.save(User(firstName = "Artem", lastName = "Kyrylenko", email = "admin@example.com", passwordHash = passwordEncoder.encode("Pass123$")!!, role = UserRoles.ADMIN))
        val u1 = userRepository.save(User(firstName = "Dmitry", lastName = "Petrenko", email = "u1@example.com", passwordHash = passwordEncoder.encode("Pass123$")!!, role = UserRoles.USER))
        val u2 = userRepository.save(User(firstName = "Yaroslav", lastName = "Shevchenko", email = "u2@example.com", passwordHash = passwordEncoder.encode("Pass123$")!!, role = UserRoles.USER))
        val u3 = userRepository.save(User(firstName = "Kateryna", lastName = "Kovalenko", email = "u3@example.com", passwordHash = passwordEncoder.encode("Pass123$")!!, role = UserRoles.USER))
        val u4 = userRepository.save(User(firstName = "Oleksandr", lastName = "Moroz", email = "u4@example.com", passwordHash = passwordEncoder.encode("Pass123$")!!, role = UserRoles.USER))
        val u5 = userRepository.save(User(firstName = "Anastasia", lastName = "Ivanova", email = "u5@example.com", passwordHash = passwordEncoder.encode("Pass123$")!!, role = UserRoles.USER))
        println("Users seeded: admin (Artem), u1-u5 (Dmitry, Yaroslav, Kateryna, Oleksandr, Anastasia).")

        // 4. Seed Topics
        val topicBeginners = problemTopicRepository.save(ProblemTopic(name = "For Beginners"))
        val topicBruteForce = problemTopicRepository.save(ProblemTopic(name = "Brute Force"))
        val topicCombinatorics = problemTopicRepository.save(ProblemTopic(name = "Combinatorics"))
        val topicDP = problemTopicRepository.save(ProblemTopic(name = "Dynamic Programming"))
        val topicGraphs = problemTopicRepository.save(ProblemTopic(name = "Graphs"))
        println("Topics seeded: For Beginners, Brute Force, Combinatorics, Dynamic Programming, Graphs.")

        // 5. Seed Script Checkers
        val indexChecker = scriptCheckerRepository.save(ScriptChecker(
            name = "Index Checker",
            language = pythonLang,
            code = """
                try:
                    user_out = int(input().strip())
                    parts = input().strip().split()
                    n = int(parts[0])
                    x = int(parts[1])
                    arr = list(map(int, input().strip().split()))
                    expected = input().strip()
                    if x not in arr:
                        if user_out == -1:
                            print("True")
                        else:
                            print("False")
                    else:
                        if user_out >= 0 and user_out < n and arr[user_out] == x:
                            print("True")
                        else:
                            print("False")
                except Exception:
                    print("False")
            """.trimIndent()
        ))

        val divisorChecker = scriptCheckerRepository.save(ScriptChecker(
            name = "Divisor Checker",
            language = pythonLang,
            code = """
                try:
                    user_out = int(input().strip())
                    n = int(input().strip())
                    expected = int(input().strip())
                    if user_out > 1 and user_out < n and n % user_out == 0:
                        print("True")
                    else:
                        print("False")
                except Exception:
                    print("False")
            """.trimIndent()
        ))
        println("Script checkers seeded.")

        // Helper to save problems
        fun createProblem(
            title: String,
            topic: ProblemTopic,
            difficulty: ProblemDifficulty,
            statement: String,
            timeLimitMs: Int = 1000,
            memoryLimitKb: Int = 64 * 1024,
            evaluationType: EvaluationType = EvaluationType.EXACT_MATCH,
            defaultChecker: ScriptChecker? = null,
            isPublic: Boolean = true,
            testGenerator: (Problem) -> List<Test>
        ): Problem {
            val problem = problemRepository.save(Problem(
                title = title,
                topic = topic,
                difficulty = difficulty,
                statement = statement,
                executionTimeLimitMs = timeLimitMs,
                executionMemoryLimitKb = memoryLimitKb,
                defaultEvaluationType = evaluationType,
                defaultScriptChecker = defaultChecker,
                isPublic = isPublic
            ))
            val tests = testGenerator(problem)
            testRepository.saveAll(tests)
            problem.tests.addAll(tests)
            return problemRepository.save(problem)
        }

        // 6. Seed Problems and Tests
        val publicProblems = mutableListOf<Problem>()

        // Problem 1: Simple A+B
        publicProblems.add(createProblem(
            title = "Simple A+B",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Given two integers A and B, find their sum.
                # Input
                A single line containing two integers A and B (-1000 ≤ A, B ≤ 1000).
                # Output
                Output the sum of A and B.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "1 1", expectedOutput = "2"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "1 2", expectedOutput = "3"))
                val extraInputs = listOf(
                    "0 0", "-10 10", "100 200", "-500 -500", "999 999", "-1000 1000",
                    "5 7", "42 -42", "123 456", "-789 123", "99 -99", "1 0", "0 1",
                    "-1 -1", "12 34", "56 78", "90 12", "34 56"
                )
                extraInputs.forEachIndexed { i, inp ->
                    val parts = inp.split(" ")
                    val sum = parts[0].toInt() + parts[1].toInt()
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = inp, expectedOutput = sum.toString()))
                }
                list
            }
        ))

        // Problem 2: Find Divisor
        publicProblems.add(createProblem(
            title = "Find Divisor",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.EASY,
            statement = """
                # Statement
                Given a composite integer N (N ≥ 4), find any of its divisors strictly between 1 and N.
                # Input
                A single line containing an integer N (4 ≤ N ≤ 10^9, N is not prime).
                # Output
                Output any divisor D of N such that 1 < D < N.
            """.trimIndent(),
            evaluationType = EvaluationType.SCRIPT_CHECK,
            defaultChecker = divisorChecker,
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "6", expectedOutput = "2"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "15", expectedOutput = "3"))
                val nums = listOf(
                    4, 8, 9, 12, 16, 20, 25, 30, 49, 100, 121, 144, 256, 500, 1000, 10000, 123456, 999999
                )
                nums.forEachIndexed { i, n ->
                    var div = 2
                    while (n % div != 0) { div++ }
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = n.toString(), expectedOutput = div.toString()))
                }
                list
            }
        ))

        // Problem 3: Find Any Index
        publicProblems.add(createProblem(
            title = "Find Any Index",
            topic = topicBruteForce,
            difficulty = ProblemDifficulty.EASY,
            statement = """
                # Statement
                Given an array of N integers and a target number X. Find any 0-based index of X in the array. If X is not present, output -1.
                # Input
                The first line contains two integers N and X (1 ≤ N ≤ 100, -100 ≤ X ≤ 100).
                The second line contains N space-separated integers.
                # Output
                Output any 0-based index i such that A[i] = X, or -1 if X does not appear in A.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "3 5\n1 2 5", expectedOutput = "2"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "4 2\n1 2 2 3", expectedOutput = "1", overrideEvaluationType = EvaluationType.SCRIPT_CHECK, overrideScriptChecker = indexChecker))
                val cases = listOf(
                    Triple("5 10\n1 2 3 4 5", "-1", false),
                    Triple("5 3\n3 1 4 1 5", "0", false),
                    Triple("5 2\n2 2 2 2 2", "0", true),
                    Triple("1 5\n5", "0", false),
                    Triple("1 5\n3", "-1", false),
                    Triple("6 7\n7 7 1 2 7 7", "0", true),
                    Triple("4 9\n9 8 7 9", "0", true),
                    Triple("5 1\n2 3 1 4 5", "2", false),
                    Triple("8 8\n1 8 2 8 3 8 4 8", "1", true),
                    Triple("3 2\n2 1 3", "0", false),
                    Triple("3 2\n1 3 2", "2", false),
                    Triple("6 5\n5 1 5 2 5 3", "0", true),
                    Triple("7 0\n0 0 0 0 0 0 0", "0", true),
                    Triple("2 4\n4 2", "0", false),
                    Triple("2 4\n2 4", "1", false),
                    Triple("2 4\n2 2", "-1", false),
                    Triple("5 -5\n1 -5 2 -5 3", "1", true),
                    Triple("4 -1\n-1 -1 -1 -1", "0", true)
                )
                cases.forEachIndexed { i, c ->
                    val evalType = if (c.third) EvaluationType.SCRIPT_CHECK else null
                    val checker = if (c.third) indexChecker else null
                    list.add(Test(
                        problem = problem,
                        ordinal = i + 3,
                        isExample = false,
                        inputData = c.first,
                        expectedOutput = c.second,
                        overrideEvaluationType = evalType,
                        overrideScriptChecker = checker
                    ))
                }
                list
            }
        ))

        // Problem 4: Watermelon
        publicProblems.add(createProblem(
            title = "Watermelon",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                One hot summer day Pete and his friend Billy decided to buy a watermelon. They chose the biggest and the ripest one, in their opinion. After that the watermelon was weighed, and the scales showed W kilos. They rushed home, dying of thirst, and decided to divide the berry, however they faced a hard problem.
                Pete and Billy are great fans of even numbers, that's why they want to divide the watermelon in such a way that each of the two parts weighs even number of kilos, at the same time it is not obligatory that the parts are equal. The boys are extremely tired and want to start their meal as soon as possible, that's why you should help them and find out, if they can divide the watermelon in the way they want. For sure, each of them should get a part of positive weight.
                # Input
                The first (and the only) input line contains integer number W (1 ≤ W ≤ 100) — the weight of the watermelon bought by the boys.
                # Output
                Print `YES`, if the boys can divide the watermelon into two parts, each of them weighing even number of kilos; and `NO` in the opposite case.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "8", expectedOutput = "YES"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "5", expectedOutput = "NO"))
                for (w in 1..18) {
                    val ans = if (w > 2 && w % 2 == 0) "YES" else "NO"
                    list.add(Test(problem = problem, ordinal = w + 2, isExample = false, inputData = w.toString(), expectedOutput = ans))
                }
                list
            }
        ))

        // Problem 5: Way Too Long Words
        publicProblems.add(createProblem(
            title = "Way Too Long Words",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Sometimes some words like "localization" or "internationalization" are so long that writing them many times in one text is quite tiresome.
                Let's consider a word too long, if its length is strictly more than 10 characters. All too long words should be replaced with a special abbreviation.
                This abbreviation is made like this: we write down the first and the last letter of a word and between them we write the number of characters between the first and the last letters. That number is in decimal system and doesn't contain any leading zeroes.
                Thus, "localization" will be spelt as "l10n", and "internationalization" will be spelt as "i18n".
                You are suggested to automatize the process of changing the words with abbreviations. All words too long should be replaced by the abbreviation and the words that are not too long should not undergo any changes.
                # Input
                The first line contains an integer N (1 ≤ N ≤ 100). Each of the following N lines contains one word. All words consist of lowercase Latin letters and share the length from 1 to 100 characters.
                # Output
                Print N lines. The i-th line should contain the result of the abbreviation of the i-th word from the input.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "4\nword\nlocalization\ninternationalization\npneumonoultramicroscopicsilicovolcanoconiosis", expectedOutput = "word\nl10n\ni18n\np43s"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "1\nsupercalifragilisticexpialidocious", expectedOutput = "s32s"))
                val wordsList = listOf(
                    listOf("apple"),
                    listOf("banana", "watermelon"),
                    listOf("a", "bb", "ccc", "dddd", "eeeee", "ffffff", "ggggggg", "hhhhhhhh", "iiiiiiiii", "jjjjjjjjjj"),
                    listOf("abcdefghijk"),
                    listOf("kubernetes", "dockerization"),
                    listOf("microservices"),
                    listOf("development"),
                    listOf("programming"),
                    listOf("engineering"),
                    listOf("java", "kotlin", "python", "javascript", "rustlang", "goprogramming"),
                    listOf("x"),
                    listOf("y".repeat(100)),
                    listOf("z".repeat(11)),
                    listOf("a".repeat(10)),
                    listOf("b".repeat(9)),
                    listOf("c".repeat(12)),
                    listOf("d".repeat(15)),
                    listOf("e".repeat(50))
                )
                wordsList.forEachIndexed { i, words ->
                    val inputStr = "${words.size}\n" + words.joinToString("\n")
                    val outputStr = words.joinToString("\n") { w ->
                        if (w.length > 10) "${w.first()}${w.length - 2}${w.last()}" else w
                    }
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = inputStr, expectedOutput = outputStr))
                }
                list
            }
        ))

        // Problem 6: Theatre Square
        publicProblems.add(createProblem(
            title = "Theatre Square",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.EASY,
            statement = """
                # Statement
                Theatre Square in the capital city of Berland has a rectangular shape with the size n x m meters. On the occasion of the city's anniversary, a decision was taken to pave the Square with square granite flagstones. Each flagstone is of the size a x a meters.
                What is the least number of flagstones needed to pave the Square? It's allowed to pave the larger surface than the Theatre Square, but the Square has to be covered. It's not allowed to break the flagstones. The sides of flagstones should be parallel to the sides of the Square.
                # Input
                The input contains three positive integers on a single line: n, m and a (1 ≤ n, m, a ≤ 10^9).
                # Output
                Write the needed number of flagstones.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "6 6 4", expectedOutput = "4"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "1 1 1", expectedOutput = "1"))
                val testInputs = listOf(
                    "1 1 2", "2 2 1", "3 5 2", "10 10 3", "100 100 1", "12 34 56", "1000 1000 1000",
                    "5 5 2", "5 5 3", "5 5 5", "5 5 6", "1000000000 1000000000 1", "1000000000 1000000000 1000000000",
                    "999999999 999999999 2", "1 1000000000 1", "1000000000 1 1", "4 4 2", "7 7 3"
                )
                testInputs.forEachIndexed { i, inp ->
                    val parts = inp.split(" ")
                    val n = parts[0].toLong()
                    val m = parts[1].toLong()
                    val a = parts[2].toLong()
                    val ans = ((n + a - 1) / a) * ((m + a - 1) / a)
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = inp, expectedOutput = ans.toString()))
                }
                list
            }
        ))

        // Problem 7: Climbing Stairs
        publicProblems.add(createProblem(
            title = "Climbing Stairs",
            topic = topicDP,
            difficulty = ProblemDifficulty.EASY,
            statement = """
                # Statement
                You are climbing a staircase. It takes N steps to reach the top.
                Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?
                # Input
                A single integer N (1 ≤ N ≤ 45).
                # Output
                Output the number of distinct ways to climb to the top.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "2", expectedOutput = "2"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "3", expectedOutput = "3"))
                val fib = LongArray(46)
                fib[1] = 1
                fib[2] = 2
                for (i in 3..45) {
                    fib[i] = fib[i-1] + fib[i-2]
                }
                for (n in 1..18) {
                    list.add(Test(problem = problem, ordinal = n + 2, isExample = false, inputData = n.toString(), expectedOutput = fib[n].toString()))
                }
                list
            }
        ))

        // Problem 8: Unique Paths
        publicProblems.add(createProblem(
            title = "Unique Paths",
            topic = topicDP,
            difficulty = ProblemDifficulty.MEDIUM,
            statement = """
                # Statement
                There is a robot on an m x n grid. The robot is initially located at the top-left corner (i.e., grid[0][0]). The robot tries to move to the bottom-right corner (i.e., grid[m-1][n-1]). The robot can only move either down or right at any point in time.
                Given the two integers m and n, return the number of possible unique paths that the robot can take to reach the bottom-right corner.
                # Input
                A single line containing two integers m and n (1 ≤ m, n ≤ 15).
                # Output
                Output the number of unique paths.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "3 7", expectedOutput = "28"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "3 2", expectedOutput = "3"))
                val cases = listOf(
                    "1 1", "1 10", "10 1", "2 2", "3 3", "4 4", "5 5", "6 6",
                    "2 8", "8 2", "3 4", "4 3", "5 2", "2 5", "10 10", "12 12", "15 15", "5 10"
                )
                fun solvePaths(m: Int, n: Int): Long {
                    val dp = Array(m) { LongArray(n) { 1L } }
                    for (i in 1 until m) {
                        for (j in 1 until n) {
                            dp[i][j] = dp[i-1][j] + dp[i][j-1]
                        }
                    }
                    return dp[m-1][n-1]
                }
                cases.forEachIndexed { i, c ->
                    val parts = c.split(" ")
                    val mVal = parts[0].toInt()
                    val nVal = parts[1].toInt()
                    val ans = solvePaths(mVal, nVal)
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = c, expectedOutput = ans.toString()))
                }
                list
            }
        ))

        // Problem 9: Combinations (nCr)
        publicProblems.add(createProblem(
            title = "Combinations (nCr)",
            topic = topicCombinatorics,
            difficulty = ProblemDifficulty.EASY,
            statement = """
                # Statement
                Given two integers N and R, compute the number of ways to choose R elements from a set of N elements (combinations, C(N, R)) modulo 10^9+7.
                # Input
                A single line containing two integers N and R (0 ≤ R ≤ N ≤ 1000).
                # Output
                Output the combinations count modulo 10^9+7.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "5 2", expectedOutput = "10"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "10 3", expectedOutput = "120"))
                val c = Array(1001) { LongArray(1001) }
                val mod = 1000000007L
                for (i in 0..1000) {
                    c[i][0] = 1
                    for (j in 1..i) {
                        c[i][j] = (c[i-1][j-1] + c[i-1][j]) % mod
                    }
                }
                val inputs = listOf(
                    "0 0", "1 0", "1 1", "5 0", "5 5", "100 50", "200 10", "500 250", "1000 500",
                    "1000 0", "1000 1000", "50 5", "100 1", "100 99", "123 45", "789 123", "999 1", "999 998"
                )
                inputs.forEachIndexed { i, inp ->
                    val parts = inp.split(" ")
                    val nVal = parts[0].toInt()
                    val rVal = parts[1].toInt()
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = inp, expectedOutput = c[nVal][rVal].toString()))
                }
                list
            }
        ))

        // Problem 10: Next Round
        publicProblems.add(createProblem(
            title = "Next Round",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                "Contestant who earns a score equal to or greater than the k-th place finisher's score will advance to the next round, as long as the contestant earns a positive score..." — an excerpt from contest rules.
                A total of N participants took part in the contest (N ≥ k), and you already know their scores. Calculate how many participants will advance to the next round.
                # Input
                The first line contains two integers N and k (1 ≤ k ≤ N ≤ 50) separated by a space.
                The second line contains N space-separated integers a_1, a_2, ..., a_N (0 ≤ a_i ≤ 100), where a_i is the score earned by the participant who got the i-th place. The given sequence is non-increasing (that is, for all i from 1 to N-1 the following condition is fulfilled: a_i ≥ a_{i+1}).
                # Output
                Output the number of participants who advance to the next round.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "8 5\n10 9 8 7 7 7 5 5", expectedOutput = "6"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "4 2\n0 0 0 0", expectedOutput = "0"))
                val testCases = listOf(
                    "5 3\n5 4 3 2 1" to "3",
                    "5 3\n5 4 0 0 0" to "2",
                    "5 5\n1 1 1 1 1" to "5",
                    "5 1\n0 0 0 0 0" to "0",
                    "6 3\n3 3 3 0 0 0" to "3",
                    "6 3\n3 3 3 3 0 0" to "4",
                    "1 1\n0" to "0",
                    "1 1\n5" to "1",
                    "10 5\n10 10 10 10 10 10 10 10 10 10" to "10",
                    "10 5\n10 9 8 7 6 5 4 3 2 1" to "5",
                    "10 5\n10 9 8 7 0 0 0 0 0 0" to "4",
                    "8 3\n8 8 8 8 8 0 0 0" to "5",
                    "7 4\n5 5 5 5 4 3 2" to "4",
                    "7 4\n5 5 5 5 5 5 5" to "7",
                    "3 2\n5 5 0" to "2",
                    "3 2\n0 0 0" to "0",
                    "4 3\n2 1 1 0" to "3",
                    "4 3\n2 2 0 0" to "2"
                )
                testCases.forEachIndexed { i, pair ->
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = pair.first, expectedOutput = pair.second))
                }
                list
            }
        ))

        // Problem 11: Team
        publicProblems.add(createProblem(
            title = "Team",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                One day three best friends Petya, Vasya and Tonya decided to form a team and take part in programming contests. Participants are usually offered several problems during a programming contest. Friend will write the solution only if they are sure in the solution. Petya, Vasya and Tonya will write a solution to a problem if at least two of them are sure. Otherwise, they won't write the solution.
                Find the number of problems the friends will solve.
                # Input
                The first line contains integer N (1 ≤ N ≤ 1000) — the number of problems in the contest.
                Then N lines contain three integers each, each integer is either 0 or 1.
                # Output
                Print a single integer — the number of problems the friends will solve.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "3\n1 1 0\n1 1 1\n1 0 0", expectedOutput = "2"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "2\n1 0 0\n0 1 0", expectedOutput = "0"))
                val r = Random(123)
                for (t in 3..20) {
                    val n = r.nextInt(10) + 1
                    val lines = mutableListOf<String>()
                    var count = 0
                    for (j in 0 until n) {
                        val p = r.nextInt(2)
                        val v = r.nextInt(2)
                        val k = r.nextInt(2)
                        lines.add("$p $v $k")
                        if (p + v + k >= 2) count++
                    }
                    list.add(Test(problem = problem, ordinal = t, isExample = false, inputData = "$n\n" + lines.joinToString("\n"), expectedOutput = count.toString()))
                }
                list
            }
        ))

        // Problem 12: Bit++
        publicProblems.add(createProblem(
            title = "Bit++",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                The language Bit++ has one variable, called X. The value of X is initially 0.
                There are 4 operations: `++X` (adds 1 to X), `X++` (adds 1 to X), `--X` (subtracts 1 from X), `X--` (subtracts 1 from X).
                Given a program in Bit++, output the final value of X.
                # Input
                The first line contains a single integer N (1 ≤ N ≤ 150) — the number of statements.
                Each of the next N lines contains exactly one statement of the form ++X, X++, --X, or X--.
                # Output
                Print a single integer — the final value of X.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "1\n++X", expectedOutput = "1"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "2\nX++\n--X", expectedOutput = "0"))
                val ops = listOf("++X", "X++", "--X", "X--")
                val r = Random(456)
                for (t in 3..20) {
                    val n = r.nextInt(15) + 1
                    val statements = mutableListOf<String>()
                    var x = 0
                    for (j in 0 until n) {
                        val op = ops[r.nextInt(4)]
                        statements.add(op)
                        if (op.contains("++")) x++ else x--
                    }
                    list.add(Test(problem = problem, ordinal = t, isExample = false, inputData = "$n\n" + statements.joinToString("\n"), expectedOutput = x.toString()))
                }
                list
            }
        ))

        // Problem 13: Beautiful Matrix
        publicProblems.add(createProblem(
            title = "Beautiful Matrix",
            topic = topicBruteForce,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                You are given a 5 x 5 matrix, consisting of 24 zeroes and a single number one. Let's index the matrix rows by numbers from 1 to 5 from top to bottom, let's index the matrix columns by numbers from 1 to 5 from left to right. In one move, you are allowed to apply one of the two following transformations to the matrix:
                Swap two neighboring rows.
                Swap two neighboring columns.
                Find the minimum number of moves needed to make the matrix beautiful. A matrix is beautiful if the single number one is in the center (row 3, column 3).
                # Input
                The input consists of 5 lines, each line contains 5 integers separated by spaces. All elements of the matrix except one are equal to 0, that element is 1.
                # Output
                Print a single integer — the minimum number of moves.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "0 0 0 0 0\n0 0 0 0 1\n0 0 0 0 0\n0 0 0 0 0\n0 0 0 0 0", expectedOutput = "3"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "0 0 0 0 0\n0 0 0 0 0\n0 0 1 0 0\n0 0 0 0 0\n0 0 0 0 0", expectedOutput = "0"))
                var ord = 3
                for (r in 0..4) {
                    for (c in 0..4) {
                        if (r == 1 && c == 4) continue // skip example 1
                        if (r == 2 && c == 2) continue // skip example 2
                        val rows = Array(5) { IntArray(5) { 0 } }
                        rows[r][c] = 1
                        val inputData = rows.joinToString("\n") { it.joinToString(" ") }
                        val expected = Math.abs(r - 2) + Math.abs(c - 2)
                        list.add(Test(problem = problem, ordinal = ord++, isExample = false, inputData = inputData, expectedOutput = expected.toString()))
                    }
                }
                list
            }
        ))

        // Problem 14: Petya and Strings
        publicProblems.add(createProblem(
            title = "Petya and Strings",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Little Petya loves presents. His mum bought him two strings of the same size for his birthday. The strings consist of uppercase and lowercase Latin letters. Now Petya wants to compare those two strings lexicographically. The letters' case does not matter, that is an uppercase letter is considered equivalent to the corresponding lowercase letter. Help Petya perform the comparison.
                # Input
                Each of the first two lines contains a string. The strings consist of uppercase and lowercase Latin letters. The strings are of the same length strictly between 1 and 100 characters.
                # Output
                If the first string is less than the second one, print "-1". If the second string is less than the first one, print "1". If the strings are equal, print "0".
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "aaaa\naaaa", expectedOutput = "0"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "abs\nAbz", expectedOutput = "-1"))
                val pairs = listOf(
                    "abc\nABC" to "0",
                    "abcdef\nabcdeg" to "-1",
                    "abcdeg\nabcdef" to "1",
                    "a\nA" to "0",
                    "A\nb" to "-1",
                    "b\nA" to "1",
                    "pEtyA\npetya" to "0",
                    "apple\nBANANA" to "-1",
                    "banana\nAPPLE" to "1",
                    "hello\nworld" to "-1",
                    "world\nhello" to "1",
                    "test\nTEST" to "0",
                    "kotlin\nKOTLIN" to "0",
                    "java\nkotlin" to "-1",
                    "python\njava" to "1",
                    "aaaaa\naaaa" to "1",
                    "xyz\nXYZ" to "0",
                    "XYZ\nxyz" to "0"
                )
                pairs.forEachIndexed { i, p ->
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = p.first, expectedOutput = p.second))
                }
                list
            }
        ))

        // Problem 15: Helpful Maths
        publicProblems.add(createProblem(
            title = "Helpful Maths",
            topic = topicBruteForce,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Xenia the beginner mathematician is a third year student at elementary school. She is now learning addition.
                To make the calculation easier, the sum only contains numbers 1, 2 and 3. Still, that isn't enough for Xenia. She only knows how to calculate the sum if the summands follow a non-decreasing order. For example, she can't calculate 1+3+2+1 but she can calculate 1+1+2+3.
                You are given the sum that was written on the board. Rearrange the summands and print the sum in such a way that Xenia can calculate it.
                # Input
                The input contains a single non-empty string S. This string consists of digits "1", "2" and "3" and characters "+".
                # Output
                Print the new sum that Xenia can calculate.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "3+2+1", expectedOutput = "1+2+3"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "1+1+3+1+3", expectedOutput = "1+1+1+3+3"))
                val cases = listOf(
                    "2", "1", "3", "1+2", "2+1", "3+3", "2+3+1", "1+1+1", "3+2+1+3+2+1",
                    "3+3+3+2+2+2+1+1+1", "1+2+3+1+2+3", "1+3+2+1+3+2", "3+1", "2+2", "2+2+1+3",
                    "3+2+2+1+1", "1+1+1+1+1+3", "3+3+3+3+3+3+1"
                )
                cases.forEachIndexed { i, c ->
                    val expected = c.split("+").sorted().joinToString("+")
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = c, expectedOutput = expected))
                }
                list
            }
        ))

        // Problem 16: Word Capitalization
        publicProblems.add(createProblem(
            title = "Word Capitalization",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Capitalization is writing a word with its first letter as a capital letter. Your task is to capitalize the given word.
                Note, that during capitalization all the letters except the first one remains unchanged.
                # Input
                A single line contains a word S. This word consists of lowercase and uppercase Latin letters and has a length from 1 to 1000 characters.
                # Output
                Print the capitalized word.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "ApPLe", expectedOutput = "ApPLe"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "konjac", expectedOutput = "Konjac"))
                val words = listOf(
                    "a", "B", "word", "Word", "capitalization", "Capitalization",
                    "kOtLiN", "jAvA", "pYtHoN", "c++", "c", "xYz", "XyZ",
                    "helloWorld", "HelloWorld", "aPple", "bAnana", "wAtermelon"
                )
                words.forEachIndexed { i, w ->
                    val expected = w[0].uppercase() + w.substring(1)
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = w, expectedOutput = expected))
                }
                list
            }
        ))

        // Problem 17: Boy or Girl
        publicProblems.add(createProblem(
            title = "Boy or Girl",
            topic = topicBruteForce,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Those days, many boys use beautiful photos as head portraits on forums, so it is pretty hard to tell the gender of a user at the first glance. Our hero Rodde and his friends had a conversation, and he suggested that if the number of distinct characters in one's username is odd, then he is a male, otherwise she is a female.
                Given the username, please help them to determine the gender of the user according to Rodde's method.
                # Input
                A single line contains a non-empty string, representing the username. The string consists of lowercase English letters.
                # Output
                If it is a female, print "CHAT WITH HER" (without quotes), otherwise print "IGNORE HIM".
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "wjmzbmr", expectedOutput = "CHAT WITH HER"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "xiaodao", expectedOutput = "IGNORE HIM"))
                val cases = listOf(
                    "sevenseven" to "IGNORE HIM",
                    "seven" to "IGNORE HIM",
                    "codeforces" to "CHAT WITH HER",
                    "a" to "IGNORE HIM",
                    "ab" to "CHAT WITH HER",
                    "abc" to "IGNORE HIM",
                    "abcd" to "CHAT WITH HER",
                    "abcde" to "IGNORE HIM",
                    "abcdef" to "CHAT WITH HER",
                    "abcdefg" to "IGNORE HIM",
                    "abcdefgh" to "CHAT WITH HER",
                    "abcdefghi" to "IGNORE HIM",
                    "abcdefghij" to "CHAT WITH HER",
                    "abcdefghijk" to "IGNORE HIM",
                    "abcdefghijkl" to "CHAT WITH HER",
                    "abcdefghijklm" to "IGNORE HIM",
                    "abcdefghijklmn" to "CHAT WITH HER",
                    "abcdefghijklmno" to "IGNORE HIM"
                )
                cases.forEachIndexed { i, p ->
                    val dist = p.first.toSet().size
                    val ans = if (dist % 2 == 0) "CHAT WITH HER" else "IGNORE HIM"
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = p.first, expectedOutput = ans))
                }
                list
            }
        ))

        // Problem 18: Soldier and Bananas
        publicProblems.add(createProblem(
            title = "Soldier and Bananas",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                A soldier wants to buy W bananas in the shop. He has to pay k dollars for the first banana, 2k dollars for the second one and so on (in other words, he has to pay i * k dollars for the i-th banana).
                He has n dollars. How many dollars does he have to borrow from his friend soldier to buy W bananas?
                # Input
                The first line contains three positive integers k, n, W (1 ≤ k, W ≤ 1000, 0 ≤ n ≤ 10^9), the cost of the first banana, initial number of dollars the soldier has and number of bananas he wants.
                # Output
                Output one integer — the amount of dollars the soldier must borrow from his friend. If he doesn't have to borrow any money, output 0.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "3 17 4", expectedOutput = "13"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "1 10 2", expectedOutput = "0"))
                val inputs = listOf(
                    "3 17 4", "1 10 2", "5 10 3", "2 10 5", "10 0 10", "100 100 1", "100 100 2",
                    "1 100 10", "10 100 10", "10 100 2", "1 0 100", "2 50 10", "3 100 10", "5 500 20",
                    "1 1000000000 10", "1000 1000 1000", "5 100 5", "5 100 6"
                )
                inputs.forEachIndexed { i, inp ->
                    val parts = inp.split(" ")
                    val kVal = parts[0].toLong()
                    val nVal = parts[1].toLong()
                    val wVal = parts[2].toLong()
                    val cost = kVal * wVal * (wVal + 1) / 2
                    val borrow = Math.max(0, cost - nVal)
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = inp, expectedOutput = borrow.toString()))
                }
                list
            }
        ))

        // Problem 19: Elephant
        publicProblems.add(createProblem(
            title = "Elephant",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                An elephant decides to visit his friend. It turned out that the elephant's house is located at point 0 and his friend's house is located at point X (X > 0) of the coordinate line. In one step the elephant can move 1, 2, 3, 4 or 5 positions forward. Determine, what is the minimum number of steps he needs to make in order to reach his friend's house.
                # Input
                The first line of the input contains an integer X (1 ≤ X ≤ 10^6) — The coordinate of the friend's house.
                # Output
                Print the minimum number of steps.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "5", expectedOutput = "1"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "12", expectedOutput = "3"))
                for (x in 1..18) {
                    val ans = (x + 4) / 5
                    list.add(Test(problem = problem, ordinal = x + 2, isExample = false, inputData = x.toString(), expectedOutput = ans.toString()))
                }
                list
            }
        ))

        // Problem 20: Wrong Subtraction
        publicProblems.add(createProblem(
            title = "Wrong Subtraction",
            topic = topicBeginners,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Little Girl Tanya is learning how to decrease a number by one, but she does it wrong with a number consisting of two or more digits. She performs the following algorithm:
                If the last digit of the number is non-zero, she decreases the number by one.
                If the last digit of the number is zero, she divides the number by 10 (i.e. removes the last digit).
                You are given an integer number N. Tanya will subtract one from it K times. Your task is to print the result after all K subtractions.
                # Input
                The first line of the input contains two integers N and K (2 ≤ N ≤ 10^9, 1 ≤ K ≤ 50).
                # Output
                Print one integer — the result of the decreasing N by one K times.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "512 4", expectedOutput = "50"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "1000000000 9", expectedOutput = "1"))
                val cases = listOf(
                    "10 1", "9 1", "100 2", "1000 3", "34 5", "12345 10", "12345 5", "1000000000 5",
                    "42 2", "42 3", "50 1", "50 2", "51 1", "51 2", "51 3", "1000 1", "1000 2", "1000 4"
                )
                cases.forEachIndexed { i, c ->
                    val parts = c.split(" ")
                    var nVal = parts[0].toLong()
                    val kVal = parts[1].toInt()
                    for (op in 0 until kVal) {
                        if (nVal % 10 == 0L) nVal /= 10 else nVal--
                    }
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = c, expectedOutput = nVal.toString()))
                }
                list
            }
        ))

        // Problem 21: Almost Lucky Number
        publicProblems.add(createProblem(
            title = "Almost Lucky Number",
            topic = topicBruteForce,
            difficulty = ProblemDifficulty.VERY_EASY,
            statement = """
                # Statement
                Petya loves lucky numbers. We all know that lucky numbers are the positive integers whose decimal representation contains only the lucky digits 4 and 7. For example, numbers 47, 744, 4 are lucky and 5, 17, 467 are not.
                Petya calls a number almost lucky if the number of lucky digits in it is a lucky number.
                # Input
                The single line contains an integer N (1 ≤ N ≤ 10^18).
                # Output
                Print "YES" if N is almost lucky, and "NO" otherwise.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "40047", expectedOutput = "NO"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "7747774", expectedOutput = "YES"))
                val cases = listOf(
                    "4", "7", "4444", "7777777", "4444777", "1234567", "47", "74", "1000000000000000",
                    "4444444", "7777", "47474747", "4747474", "44477744477744", "4447774447774", "14273467", "477", "774"
                )
                cases.forEachIndexed { i, c ->
                    val count = c.count { it == '4' || it == '7' }
                    val ans = if (count == 4 || count == 7) "YES" else "NO"
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = c, expectedOutput = ans))
                }
                list
            }
        ))

        // Problem 22: Two Sum
        publicProblems.add(createProblem(
            title = "Two Sum",
            topic = topicBruteForce,
            difficulty = ProblemDifficulty.EASY,
            statement = """
                # Statement
                Given a 1-indexed sorted array of N integers and a target value T. Find the indices of the two elements that sum up to T. There is exactly one unique solution.
                # Input
                The first line contains T (2 ≤ T ≤ 2 * 10^9).
                The second line contains space-separated elements of the sorted array (2 ≤ N ≤ 1000).
                # Output
                Output two space-separated 1-based indices of the elements.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "9\n2 7 11 15", expectedOutput = "1 2"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "6\n3 3", expectedOutput = "1 2"))
                val cases = listOf(
                    "10\n1 2 3 4 5 6 7 8 9" to "2 8",
                    "100\n10 20 30 40 50 60 70 80 90" to "4 6",
                    "5\n2 3" to "1 2",
                    "8\n2 4 6" to "1 3",
                    "0\n-3 -2 -1 0 1 2 3" to "1 7",
                    "2\n-1 0 1 2 3" to "1 5",
                    "10\n1 9" to "1 2",
                    "10\n2 8" to "1 2",
                    "10\n3 7" to "1 2",
                    "10\n4 6" to "1 2",
                    "10\n5 5" to "1 2",
                    "15\n5 10" to "1 2",
                    "20\n8 12" to "1 2",
                    "30\n10 20" to "1 2",
                    "40\n15 25" to "1 2",
                    "50\n20 30" to "1 2",
                    "60\n25 35" to "1 2",
                    "70\n30 40" to "1 2"
                )
                cases.forEachIndexed { i, p ->
                    list.add(Test(problem = problem, ordinal = i + 3, isExample = false, inputData = p.first, expectedOutput = p.second))
                }
                list
            }
        ))

        // Problem 23: Graph Degrees
        publicProblems.add(createProblem(
            title = "Graph Degrees",
            topic = topicGraphs,
            difficulty = ProblemDifficulty.EASY,
            statement = """
                # Statement
                Given an undirected graph with N vertices and M edges. For each vertex from 1 to N, find its degree (the number of edges connected to it).
                # Input
                The first line contains two integers N and M (1 ≤ N ≤ 100, 0 ≤ M ≤ 1000).
                Each of the next M lines contains two integers u and v (1 ≤ u, v ≤ N) representing an edge.
                # Output
                Output a single line containing N space-separated integers, where the i-th integer is the degree of vertex i.
            """.trimIndent(),
            testGenerator = { problem ->
                val list = mutableListOf<Test>()
                list.add(Test(problem = problem, ordinal = 1, isExample = true, inputData = "3 2\n1 2\n2 3", expectedOutput = "1 2 1"))
                list.add(Test(problem = problem, ordinal = 2, isExample = true, inputData = "4 0", expectedOutput = "0 0 0 0"))
                val r = Random(42)
                for (t in 3..20) {
                    val n = r.nextInt(15) + 3
                    val m = r.nextInt(20)
                    val degrees = IntArray(n)
                    val edges = mutableListOf<String>()
                    for (e in 0 until m) {
                        val u = r.nextInt(n) + 1
                        val v = r.nextInt(n) + 1
                        edges.add("$u $v")
                        degrees[u - 1]++
                        degrees[v - 1]++
                    }
                    val inputStr = "$n $m" + (if (edges.isEmpty()) "" else "\n" + edges.joinToString("\n"))
                    val expectedStr = degrees.joinToString(" ")
                    list.add(Test(problem = problem, ordinal = t, isExample = false, inputData = inputStr, expectedOutput = expectedStr))
                }
                list
            }
        ))

        println("Seeded 23 public problems with at least 20 test cases each.")

        // 7. Seed Contests
        val now = Instant.now()

        // Contest 1: Started 5 minutes ago, ends in 55 minutes
        val contest1 = contestRepository.save(Contest(
            name = "Warmup Contest",
            overview = "Warmup contest for practice and debugging. 1 hour duration, active now.",
            rules = "Standard rules apply. 20-minute scoreboard freeze at the end.",
            startTime = now.minus(5, ChronoUnit.MINUTES),
            endTime = now.plus(55, ChronoUnit.MINUTES),
            freezeTime = now.plus(35, ChronoUnit.MINUTES)
        ))

        // Contest 2: Starts in 5 minutes, ends in 1 hour 5 minutes
        val contest2 = contestRepository.save(Contest(
            name = "Algorithm Cup",
            overview = "Upcoming algorithm speed contest. 1 hour duration.",
            rules = "Standard rules apply. 20-minute scoreboard freeze at the end.",
            startTime = now.plus(5, ChronoUnit.MINUTES),
            endTime = now.plus(65, ChronoUnit.MINUTES),
            freezeTime = now.plus(45, ChronoUnit.MINUTES)
        ))

        // Register Users u3, u4, u5 in both contests
        listOf(u3, u4, u5).forEach { user ->
            contestMemberRepository.save(ContestMember(user = user, contest = contest1))
            contestMemberRepository.save(ContestMember(user = user, contest = contest2))
        }

        // Add 10 non-public problems to both contests (can duplicate public ones)
        // Ensure first problem in each contest is "Simple A+B"
        fun duplicateAsNonPublic(publicProb: Problem): Problem {
            val nonPublic = problemRepository.save(Problem(
                title = publicProb.title,
                topic = publicProb.topic,
                difficulty = publicProb.difficulty,
                statement = publicProb.statement,
                executionTimeLimitMs = publicProb.executionTimeLimitMs,
                executionMemoryLimitKb = publicProb.executionMemoryLimitKb,
                defaultEvaluationType = publicProb.defaultEvaluationType,
                defaultScriptChecker = publicProb.defaultScriptChecker,
                isPublic = false
            ))
            val duplicateTests = publicProb.tests.map { t ->
                Test(
                    problem = nonPublic,
                    ordinal = t.ordinal,
                    isExample = t.isExample,
                    inputData = t.inputData,
                    expectedOutput = t.expectedOutput,
                    overrideEvaluationType = t.overrideEvaluationType,
                    overrideScriptChecker = t.overrideScriptChecker
                )
            }
            testRepository.saveAll(duplicateTests)
            nonPublic.tests.addAll(duplicateTests)
            return problemRepository.save(nonPublic)
        }

        // Select first 10 public problems to duplicate for Contest 1 and Contest 2
        val problemsToDuplicate = publicProblems.take(10)
        
        // Contest 1 Problems
        val contest1Problems = problemsToDuplicate.mapIndexed { index, publicProb ->
            val npProb = duplicateAsNonPublic(publicProb)
            contestProblemRepository.save(ContestProblem(
                contest = contest1,
                problem = npProb,
                ordinal = index + 1
            ))
        }

        // Contest 2 Problems
        val contest2Problems = problemsToDuplicate.mapIndexed { index, publicProb ->
            val npProb = duplicateAsNonPublic(publicProb)
            contestProblemRepository.save(ContestProblem(
                contest = contest2,
                problem = npProb,
                ordinal = index + 1
            ))
        }

        // Seed Solutions
        val p1 = publicProblems[0]
        val p2 = publicProblems[1]
        val p3 = publicProblems[2]
        val p4 = publicProblems[3]
        val p5 = publicProblems[4]
        val p6 = publicProblems[5]

        // Simple A+B
        listOf(u1, u2, u3, u4, u5).forEachIndexed { index, user ->
            solutionRepository.save(Solution(
                problem = p1,
                user = user,
                code = "a, b = map(int, input().split())\nprint(a + b)",
                language = pythonLang,
                status = SolutionStatus.SUCCESS,
                sentAt = now.minus((30 + index).toLong(), ChronoUnit.MINUTES)
            ))
        }

        // Watermelon
        listOf(u1, u2, u3, u4).forEachIndexed { index, user ->
            solutionRepository.save(Solution(
                problem = p4,
                user = user,
                code = "w = int(input())\nprint('YES' if w > 2 and w % 2 == 0 else 'NO')",
                language = pythonLang,
                status = SolutionStatus.SUCCESS,
                sentAt = now.minus((20 + index).toLong(), ChronoUnit.MINUTES)
            ))
        }

        // Way Too Long Words
        listOf(u1, u2, u3).forEachIndexed { index, user ->
            solutionRepository.save(Solution(
                problem = p5,
                user = user,
                code = "n = int(input())\nfor _ in range(n):\n    s = input()\n    print(s[0] + str(len(s)-2) + s[-1] if len(s) > 10 else s)",
                language = pythonLang,
                status = SolutionStatus.SUCCESS,
                sentAt = now.minus((15 + index).toLong(), ChronoUnit.MINUTES)
            ))
        }

        // Theatre Square
        listOf(u1, u2).forEachIndexed { index, user ->
            solutionRepository.save(Solution(
                problem = p6,
                user = user,
                code = "n, m, a = map(int, input().split())\nprint(((n + a - 1) // a) * ((m + a - 1) // a))",
                language = pythonLang,
                status = SolutionStatus.SUCCESS,
                sentAt = now.minus((10 + index).toLong(), ChronoUnit.MINUTES)
            ))
        }

        // In Progress solutions for u1
        solutionRepository.save(Solution(
            problem = p2,
            user = u1,
            code = "print(2)",
            language = pythonLang,
            status = SolutionStatus.TEST_FAILED,
            sentAt = now.minus(5L, ChronoUnit.MINUTES)
        ))

        solutionRepository.save(Solution(
            problem = p3,
            user = u1,
            code = "print(-1)",
            language = pythonLang,
            status = SolutionStatus.COMPILATION_ERROR,
            sentAt = now.minus(8L, ChronoUnit.MINUTES)
        ))

        // Contest submissions
        val cp1_1 = contest1Problems[0]
        val cp1_2 = contest1Problems[1]

        // u3
        solutionRepository.save(Solution(
            problem = cp1_1.problem,
            user = u3,
            contest = contest1,
            contestProblem = cp1_1,
            code = "a, b = map(int, input().split())\nprint(a + b)",
            language = pythonLang,
            status = SolutionStatus.SUCCESS,
            sentAt = now.minus(4L, ChronoUnit.MINUTES),
            executedAt = now.minus(4L, ChronoUnit.MINUTES)
        ))

        solutionRepository.save(Solution(
            problem = cp1_2.problem,
            user = u3,
            contest = contest1,
            contestProblem = cp1_2,
            code = "print(0)",
            language = pythonLang,
            status = SolutionStatus.TEST_FAILED,
            sentAt = now.minus(3L, ChronoUnit.MINUTES),
            executedAt = now.minus(3L, ChronoUnit.MINUTES)
        ))

        solutionRepository.save(Solution(
            problem = cp1_2.problem,
            user = u3,
            contest = contest1,
            contestProblem = cp1_2,
            code = "n = int(input())\nfor i in range(2, n):\n    if n % i == 0:\n        print(i)\n        break",
            language = pythonLang,
            status = SolutionStatus.SUCCESS,
            sentAt = now.minus(2L, ChronoUnit.MINUTES),
            executedAt = now.minus(2L, ChronoUnit.MINUTES)
        ))

        // u4
        solutionRepository.save(Solution(
            problem = cp1_1.problem,
            user = u4,
            contest = contest1,
            contestProblem = cp1_1,
            code = "a, b = map(int, input().split())\nprint(a + b)",
            language = pythonLang,
            status = SolutionStatus.SUCCESS,
            sentAt = now.minus(3L, ChronoUnit.MINUTES),
            executedAt = now.minus(3L, ChronoUnit.MINUTES)
        ))

        solutionRepository.save(Solution(
            problem = cp1_2.problem,
            user = u4,
            contest = contest1,
            contestProblem = cp1_2,
            code = "print(1)",
            language = pythonLang,
            status = SolutionStatus.TEST_FAILED,
            sentAt = now.minus(1L, ChronoUnit.MINUTES),
            executedAt = now.minus(1L, ChronoUnit.MINUTES)
        ))

        // u5
        solutionRepository.save(Solution(
            problem = cp1_1.problem,
            user = u5,
            contest = contest1,
            contestProblem = cp1_1,
            code = "a b = input()",
            language = pythonLang,
            status = SolutionStatus.COMPILATION_ERROR,
            sentAt = now.minus(2L, ChronoUnit.MINUTES),
            executedAt = now.minus(2L, ChronoUnit.MINUTES)
        ))

        // Helper to save comments
        fun saveComment(
            problem: Problem,
            user: User,
            text: String,
            parent: Comment? = null,
            createdAt: Instant = now
        ): Comment {
            val comment = commentRepository.save(Comment(
                problem = problem,
                user = user,
                text = text,
                parent = parent,
                createdAt = createdAt
            ))
            if (parent != null) {
                parent.replies.add(comment)
                commentRepository.save(parent)
            }
            return comment
        }

        // Seed Comments
        val p1_c1 = saveComment(p1, u1, "Is Python's `input().split()` slow for large inputs?", createdAt = now.minus(40L, ChronoUnit.MINUTES))
        saveComment(p1, u2, "For simple A+B it doesn't matter, but for larger problems use `sys.stdin.readline().split()`.", parent = p1_c1, createdAt = now.minus(35L, ChronoUnit.MINUTES))
        saveComment(p1, admin, "This is a basic practice task. Keep it up!", createdAt = now.minus(30L, ChronoUnit.MINUTES))

        val p4_c1 = saveComment(p4, u3, "Be careful with W = 2. It cannot be divided into two even positive parts since the only option is 1 and 1, which are odd!", createdAt = now.minus(25L, ChronoUnit.MINUTES))
        saveComment(p4, u4, "Ah! I forgot about the positive constraint and spent 10 minutes debugging why 2 failed. Thanks!", parent = p4_c1, createdAt = now.minus(22L, ChronoUnit.MINUTES))

        val p2_c1 = saveComment(p2, u1, "I'm struggling with performance for N = 10^9. Any tips?", createdAt = now.minus(18L, ChronoUnit.MINUTES))
        saveComment(p2, u5, "Try searching only up to sqrt(N)!", parent = p2_c1, createdAt = now.minus(15L, ChronoUnit.MINUTES))

        println("Contests and members seeded. 10 non-public duplicate problems added to each contest (first is Simple A+B).")
        println("=== Demo Data Initialization Completed Successfully ===")
    }
}
