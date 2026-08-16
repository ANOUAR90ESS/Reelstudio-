package com.example.data.model

object SampleDramas {

    val dramas: List<Drama> = listOf(
        Drama(
            id = "drama_billionaire_husband",
            title = "The Double Life of My Billionaire Husband",
            description = "Forced into an arranged marriage with an infamous crippled heir, Natalie expects a life in the shadows. But on their wedding night, she discovers he is neither crippled nor poor — he is the ruthless trillionaire king of Wall Street who hides his identity to seek vengeance!",
            coverGradientStart = 0xFF8A001A, // Deep Burgundy
            coverGradientEnd = 0xFF14070C,
            badge = "TOP 1",
            genre = DramaGenre.BILLIONAIRE,
            rating = 4.9f,
            viewCount = "48.6M",
            likeCount = "2.3M",
            totalEpisodes = 50,
            cast = listOf("Sebastian Hayes", "Natalie Rivera", "Victor Sterling", "Claire Fontaine"),
            director = "Elena Gomez",
            tags = listOf("Billionaire", "Secret Identity", "Arranged Marriage", "Revenge", "Contract Love"),
            episodes = (1..15).map { num ->
                Episode(
                    id = "dbh_ep_$num",
                    dramaId = "drama_billionaire_husband",
                    episodeNumber = num,
                    title = when (num) {
                        1 -> "The Humiliating Wedding"
                        2 -> "The Mask Comes Off"
                        3 -> "Penthouse Secrets"
                        4 -> "Slap at the Family Gala"
                        5 -> "The 100 Million Dollar Check"
                        6 -> "Jealous Stepsister's Trap"
                        7 -> "He Steps In To Defend Her"
                        8 -> "Whispers in the Rain"
                        9 -> "The Rival Billionaire Strikes"
                        10 -> "A Dangerous Kiss in the Dark"
                        else -> "Shocking Revelation Part $num"
                    },
                    durationSeconds = 75 + (num * 3),
                    isFree = num <= 3,
                    coinCost = 20,
                    previewSubtitle = when (num) {
                        1 -> "Natalie stands alone at the altar as whispers echo through the hall..."
                        2 -> "He stands up from his wheelchair, his piercing gaze locking onto hers."
                        3 -> "'You think I'm weak, wife? You haven't seen anything yet.'"
                        4 -> "The arrogant stepsister tries to spill wine, but Natalie catches her wrist cold."
                        5 -> "'Sign the papers, or watch your family empire crumble by midnight.'"
                        else -> "Intense showdown unfolds behind closed doors!"
                    },
                    scriptLines = listOf(
                        ScriptLine("Natalie", "They told me you were helpless... Who are you really?", 5),
                        ScriptLine("Sebastian", "I am the man who owns everything in this city. Including you.", 18),
                        ScriptLine("Natalie", "Don't come any closer! We had a contract!", 32),
                        ScriptLine("Sebastian", "In my world, contracts are made to be rewritten.", 48),
                        ScriptLine("Narrator", "To be continued in the next shocking chapter...", 65)
                    )
                )
            }
        ),
        Drama(
            id = "drama_forbidden_alpha",
            title = "Fated to My Forbidden Alpha",
            description = "Rejected by her pack and condemned as an outcast, Selene thought her life was over. Until the feared Blood Moon Alpha claims her as his true fated mate. But pack laws forbid their union, igniting a war across the territories.",
            coverGradientStart = 0xFF280B45, // Royal Violet
            coverGradientEnd = 0xFF0D061A,
            badge = "HOT",
            genre = DramaGenre.WEREWOLF,
            rating = 4.8f,
            viewCount = "34.2M",
            likeCount = "1.8M",
            totalEpisodes = 40,
            cast = listOf("Alpha Logan Vance", "Selene Nightshade", "Beta Liam", "Lady Vivienne"),
            director = "Marcus Croft",
            tags = listOf("Werewolf", "Alpha Mate", "Supernatural", "Forbidden Love", "Pack War"),
            episodes = (1..12).map { num ->
                Episode(
                    id = "ffa_ep_$num",
                    dramaId = "drama_forbidden_alpha",
                    episodeNumber = num,
                    title = when (num) {
                        1 -> "The Blood Moon Ceremony"
                        2 -> "Rejected by the Ex"
                        3 -> "The Alpha's Claim"
                        4 -> "Rival Pack Ambush"
                        5 -> "Awakening of the Silver Wolf"
                        else -> "Pack Territory War $num"
                    },
                    durationSeconds = 80 + num,
                    isFree = num <= 3,
                    coinCost = 20,
                    previewSubtitle = "The howling wind signals the arrival of the supreme Alpha.",
                    scriptLines = listOf(
                        ScriptLine("Selene", "I am nobody. Why did you choose me?", 8),
                        ScriptLine("Alpha Logan", "The moon made no mistake. You belong to me now.", 22),
                        ScriptLine("Selene", "Your council will tear this pack apart if they find out!", 40),
                        ScriptLine("Alpha Logan", "Let them try. I will burn any kingdom that stands between us.", 58)
                    )
                )
            }
        ),
        Drama(
            id = "drama_secret_heiress",
            title = "Never Divorce a Secret Heiress",
            description = "For three years, Chloe endured abuse and mockery from her husband's wealthy family as a humble housewife. The moment the divorce papers are signed, she steps out into a convoy of Rolls-Royces — revealed as the sole heiress to the world's largest conglomerate!",
            coverGradientStart = 0xFF7D1630, // Crimson Ruby
            coverGradientEnd = 0xFF170912,
            badge = "TRENDING",
            genre = DramaGenre.REVENGE,
            rating = 4.9f,
            viewCount = "52.1M",
            likeCount = "3.1M",
            totalEpisodes = 60,
            cast = listOf("Chloe Sterling", "Ethan Drake", "Sophia Vance", "Grandpa Sterling"),
            director = "Kendra Shaw",
            tags = listOf("Secret Heiress", "Slap in the Face", "Revenge", "Divorce", "Billionaire"),
            episodes = (1..15).map { num ->
                Episode(
                    id = "ndh_ep_$num",
                    dramaId = "drama_secret_heiress",
                    episodeNumber = num,
                    title = when (num) {
                        1 -> "The Cold Divorce Agreement"
                        2 -> "10 Billion Dollar Inheritance"
                        3 -> "The Charity Auction Humiliation"
                        4 -> "Ex-Husband's Shock"
                        5 -> "Bidding 50 Million on a Whim"
                        else -> "The Grand Retribution Part $num"
                    },
                    durationSeconds = 70 + (num * 2),
                    isFree = num <= 3,
                    coinCost = 20,
                    previewSubtitle = "Ethan sneers as Chloe puts down her pen on the divorce deed.",
                    scriptLines = listOf(
                        ScriptLine("Ethan", "Take this ten thousand dollars and never show your face in high society again.", 6),
                        ScriptLine("Chloe", "Keep it for your bankruptcy fees. You'll need every penny by tomorrow morning.", 24),
                        ScriptLine("Butler", "Welcome home, Lady Chloe. The board awaits your command.", 42),
                        ScriptLine("Ethan", "Wait... Lady Chloe?! That's impossible!", 59)
                    )
                )
            }
        ),
        Drama(
            id = "drama_triplets_ceo",
            title = "The CEO's Hidden Triplets",
            description = "Five years ago, Maya was betrayed and left destitute in Paris. Today, she returns as a renowned tech genius with three adorable super-genius triplets who hack the biggest empire in Manhattan to find their daddy!",
            coverGradientStart = 0xFF134E5E, // Deep Emerald Cyan
            coverGradientEnd = 0xFF0B192C,
            badge = "SWEET",
            genre = DramaGenre.SWEET_LOVE,
            rating = 4.7f,
            viewCount = "29.4M",
            likeCount = "1.5M",
            totalEpisodes = 45,
            cast = listOf("Maya Lin", "Alexander Blackwood", "Leo", "Mia", "Toby"),
            director = "Sam Harrison",
            tags = listOf("Genius Kids", "Billionaire Dad", "Reunion", "Romantic Comedy", "Family"),
            episodes = (1..10).map { num ->
                Episode(
                    id = "cht_ep_$num",
                    dramaId = "drama_triplets_ceo",
                    episodeNumber = num,
                    title = when (num) {
                        1 -> "Three Cute Hackers Attack"
                        2 -> "Daddy, Meet Your Boss!"
                        3 -> "DNA Test Panic"
                        4 -> "Moving into the Mansion"
                        else -> "Parenting the Billionaire Part $num"
                    },
                    durationSeconds = 72 + num,
                    isFree = num <= 3,
                    coinCost = 20,
                    previewSubtitle = "Mini-genius Leo taps enter on his laptop and locks down Blackwood Tower.",
                    scriptLines = listOf(
                        ScriptLine("Leo", "Mommy said our dad is a handsome billionaire with zero emotional intelligence.", 10),
                        ScriptLine("Alexander", "Who gave these three five-year-olds root access to my security mainframe?!", 28),
                        ScriptLine("Mia", "Hi Daddy! Do you like our greeting card?", 45)
                    )
                )
            }
        ),
        Drama(
            id = "drama_contract_bride",
            title = "The Alpha King's Contract Bride",
            description = "To save her ailing sister, Clara enters into a 100-day contract with the ruthless underground boss of the city. But the dark secrets between their pasts will either unite them or destroy everyone they love.",
            coverGradientStart = 0xFF4A0E17,
            coverGradientEnd = 0xFF140508,
            badge = "VIP",
            genre = DramaGenre.ROMANCE,
            rating = 4.8f,
            viewCount = "21.9M",
            likeCount = "920K",
            totalEpisodes = 35,
            cast = listOf("Clara Montgomery", "Dominic Castillo", "Isabella King"),
            director = "Helena Vance",
            tags = listOf("Contract Marriage", "Mafia Boss", "Emotional Romance", "Redemption"),
            episodes = (1..8).map { num ->
                Episode(
                    id = "ckb_ep_$num",
                    dramaId = "drama_contract_bride",
                    episodeNumber = num,
                    title = "Episode $num: " + when (num) {
                        1 -> "Midnight Proposal"
                        2 -> "Rules of the Penthouse"
                        3 -> "The Velvet Room"
                        else -> "Heartstrings and Shadows"
                    },
                    durationSeconds = 78,
                    isFree = num <= 3,
                    coinCost = 20,
                    previewSubtitle = "Dominic slides the velvet box across the mahogany table."
                )
            }
        ),
        Drama(
            id = "drama_rebirth_revenge",
            title = "Rebirth: Revenge of the Fallen Heiress",
            description = "Poisoned on her 25th birthday by her treacherous fiancé and greedy cousin, Victoria opens her eyes to find herself back in time three years before the tragedy. Armed with memories of the future, she dismantles their schemes one by one!",
            coverGradientStart = 0xFF531253, // Dark Magenta
            coverGradientEnd = 0xFF150519,
            badge = "NEW",
            genre = DramaGenre.REVENGE,
            rating = 4.9f,
            viewCount = "38.7M",
            likeCount = "2.1M",
            totalEpisodes = 55,
            cast = listOf("Victoria Thorne", "Julian Cross", "Evelyn Frost"),
            director = "Arthur Pendelton",
            tags = listOf("Rebirth", "Time Travel", "Mastermind", "Sweet Retribution", "Royalty"),
            episodes = (1..10).map { num ->
                Episode(
                    id = "rrr_ep_$num",
                    dramaId = "drama_rebirth_revenge",
                    episodeNumber = num,
                    title = "Episode $num: " + when (num) {
                        1 -> "Waking Up 3 Years Ago"
                        2 -> "The Poisoned Wine Cup"
                        3 -> "Aligning with the Dark Duke"
                        else -> "Retribution Phase $num"
                    },
                    durationSeconds = 82,
                    isFree = num <= 3,
                    coinCost = 20,
                    previewSubtitle = "Victoria smiles softly as her cousin hands her the poisoned glass, knowing exactly what to do."
                )
            }
        ),
        Drama(
            id = "drama_night_stalker_ceo",
            title = "Shadows of Manhattan: The CEO's Secret",
            description = "By day, Damian is the coldest tech tycoon in the United States. By night, he leads an elite vigilante task force investigating a dark conspiracy that killed his father. When investigative journalist Ava stumbles into his crosshairs, their fates collide.",
            coverGradientStart = 0xFF0D253A, // Deep Navy Slate
            coverGradientEnd = 0xFF050B13,
            badge = "SUSPENSE",
            genre = DramaGenre.SUSPENSE,
            rating = 4.8f,
            viewCount = "17.3M",
            likeCount = "750K",
            totalEpisodes = 30,
            cast = listOf("Damian Vance", "Ava Sinclair", "Detective Raymond"),
            director = "Kenji Sato",
            tags = listOf("Suspense", "Secret Identity", "Action", "Thriller", "Modern Mystery"),
            episodes = (1..8).map { num ->
                Episode(
                    id = "smc_ep_$num",
                    dramaId = "drama_night_stalker_ceo",
                    episodeNumber = num,
                    title = "Episode $num: " + when (num) {
                        1 -> "Rooftop Encounter"
                        2 -> "The Encrypted USB"
                        3 -> "Undercover at the Gala"
                        else -> "Deadly Pursuit"
                    },
                    durationSeconds = 85,
                    isFree = num <= 3,
                    coinCost = 20,
                    previewSubtitle = "Sirens wail through the rain-soaked streets as shadow figures disperse."
                )
            }
        )
    )

    val sampleComments = listOf(
        DramaComment("c1", "drama_billionaire_husband", 1, "Sarah_RomanceLover", 0xFFE91E63, "OMG that plot twist when he stood up! I screamed! 😱🔥", "2m ago", 142),
        DramaComment("c2", "drama_billionaire_husband", 1, "DramaQueen99", 0xFF9C27B0, "Best mini series on ReelShort hands down! Ep 2 is insane!", "15m ago", 89),
        DramaComment("c3", "drama_billionaire_husband", 1, "MovieBingeAddict", 0xFF2196F3, "The actor playing Sebastian is so charismatic. Rewatching again!", "1h ago", 54),
        DramaComment("c4", "drama_billionaire_husband", 2, "ElenaVibes", 0xFFFF9800, "She caught the stepsister's hand so smoothly! Queen energy 👑", "3h ago", 204),
        DramaComment("c5", "drama_forbidden_alpha", 1, "WolfLover_X", 0xFF4CAF50, "Logan's voice is everything. Claim her already!!", "25m ago", 112),
        DramaComment("c6", "drama_secret_heiress", 1, "BillionaireFan", 0xFFE50914, "That ex-husband is going to cry blood when he finds out who she is haha!!", "42m ago", 320)
    )

    val coinStorePackages = listOf(
        CoinPackage("coin_tier_1", 200, 0, "$0.99", isPopular = false),
        CoinPackage("coin_tier_2", 500, 100, "$2.99", isPopular = false, tag = "+20% Free"),
        CoinPackage("coin_tier_3", 1000, 300, "$4.99", isPopular = true, tag = "BEST VALUE"),
        CoinPackage("coin_tier_4", 2500, 1000, "$9.99", isPopular = false, tag = "+40% Free"),
        CoinPackage("coin_tier_5", 6000, 3000, "$19.99", isPopular = false, tag = "MEGA PACK")
    )

    val defaultDailyTasks = listOf(
        DailyTask("task_daily_checkin", "Daily Check-in", "Claim your free daily login coin bonus", 50, 1, 0),
        DailyTask("task_watch_3", "Watch 3 Episodes", "Watch any 3 short drama episodes", 30, 3, 0),
        DailyTask("task_like_5", "Like 5 Episodes", "Show love to your favorite creators", 20, 5, 0),
        DailyTask("task_share_drama", "Share a Drama", "Share any drama link with friends", 40, 1, 0),
        DailyTask("task_watch_ad", "Watch Video Ad", "Earn instant coins by viewing a sponsor clip", 50, 1, 0)
    )
}
