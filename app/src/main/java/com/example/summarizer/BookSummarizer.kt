package com.example.summarizer

data class ChapterSummary(
    val chapterTitle: String,
    val compact: String,
    val standard: String,
    val detailed: String,
    val narrationReady: String
)

data class BookSummaryResult(
    val bookTitle: String,
    val chapters: List<ChapterSummary>,
    val fullCompact: String,
    val fullStandard: String,
    val fullDetailed: String,
    val fullNarrationReady: String
)

data class SampleBook(
    val title: String,
    val author: String,
    val text: String
)

object BookSummarizer {

    val sampleBooks = listOf(
        SampleBook(
            title = "شازده کوچولو",
            author = "آنتوان دو سنت‌اگزوپری",
            text = """
فصل اول:
وقتی شش ساله بودم، در کتابی به نام «داستان‌های واقعی» تصویری از یک مار بوآ دیدم که جانور درنده‌ای را می‌بلعید. در آن کتاب آمده بود که مارهای بوآ طعمه خود را بدون جویدن درسته قورت می‌دهند و پس از آن شش ماه به خواب می‌روند تا هضمش کنند. من نقاشی کشیدم؛ کلاهی نبود، بلکه مار بوآیی بود که فیلی را هضم می‌کرد. اما آدم‌های بزرگ متوجه نشدند و گفتند از یک کلاه نباید ترسید. آدم‌های بزرگ همیشه نیاز به توضیح دارند.

فصل دوم:
من هواپیمایم در صحرای بزرگ آفریقا خراب شد. در هزاران مایل دورتر از هر آبادی تنها بودم. صبح با صدای نازکی بیدار شدم: «خواهش می‌کنم یک بره برای من بکش!» من از جا جستم و شازده کوچولو را دیدم. پسری مو طلایی که در دل کویر گم نشده بود و نه خسته بود و نه تشنه. او سیاره کوچکی داشت که تنها سه آتشفشان و یک گل سرخ مغرور داشت. ارزش گل سرخ او به خاطر عمری بود که برایش صرف کرده بود. چشم دل است که می‌بیند، آنچه اصل است از دیده پنهان است.
            """.trimIndent()
        ),
        SampleBook(
            title = "هنر شفاف اندیشیدن",
            author = "رولف دوبلی",
            text = """
فصل اول: خطای تایید
خطای تایید یعنی مادر تمام خطاهای شناختی انسان. تمایل به این که اطلاعات جدید را طوری تعبیر کنیم که با دیدگاه‌ها، باورها و تئوری‌های موجود ما همخوانی داشته باشند. اگر شما باور داشته باشید که جهان در حال نابودی است، هر روز شواهدی برای اثباتش در اخبار می‌یابید. راه مقابله با این خطا، نوشتن عمدی باورهای خلاف عقیده خود و جستجوی شواهد نقض است.

فصل دوم: توهم کنترل
توهم کنترل تمایل به این باور است که ما می‌توانیم بر چیزی که کاملاً غیرقابل پیش‌بینی یا شانسی است اثر بگذاریم. مانند زدن دکمه‌های آسانسور به دفعات زیاد، یا بازیگرانی که تاس را محکم‌تر پرتاب می‌کنند به امید آوردن عدد شش. واقعیت این است که بخش اعظم جهان تحت تاثیر تصادف است و پذیرش عدم قطعیت اولین قدم برای آرامش روان است.
            """.trimIndent()
        ),
        SampleBook(
            title = "چهار اثر",
            author = "فلورانس اسکاول شین",
            text = """
فصل اول: بازی زندگی و چگونگی آن
بیشتر مردم زندگی را یک مبارزه تصور می‌کنند؛ اما زندگی یک بازی است. بازی بزرگی از دادن و گرفتن. قانون کارما و قانون جذب. آنچه انسان بکارد، همان را درو خواهد کرد. هر آنچه در کلام و اندیشه منتشر شود، دیر یا زود با دقت به زندگی او بازمی‌گردد. کلام تو عصای معجزه‌گر توست.

فصل دوم: راه کامیابی
ایمان حقیقی بدون ترس حرکت می‌کند. ترس تنها دشمن واقعی انسان است. ترس از نداری، ترس از شکست، ترس از تنهایی. وقتی انسان بیاموزد بار خود را به قانون الهی بسپارد و در آرامش گام بردارد، درهای بسته به روی او گشوده می‌شوند. شکرگزاری پیش از دریافت، قوی‌ترین ارتعاش موفقیت در جهان است.
            """.trimIndent()
        )
    )

    fun summarize(text: String, bookTitle: String = "کتاب صوتی"): BookSummaryResult {
        val chapterTexts = splitIntoChapters(text)
        val chapterSummaries = chapterTexts.mapIndexed { index, chText ->
            val num = index + 1
            val title = extractChapterTitle(chText, num)
            val sentences = chText.split(Regex("[.،؛\n]+"))
                .map { it.trim() }
                .filter { it.length > 10 }

            val compact = generateCompactSummary(sentences, title)
            val standard = generateStandardSummary(sentences, title)
            val detailed = generateDetailedSummary(sentences, title)
            val narrationReady = formatForNarration(standard, title)

            ChapterSummary(
                chapterTitle = title,
                compact = compact,
                standard = standard,
                detailed = detailed,
                narrationReady = narrationReady
            )
        }

        val fullCompact = chapterSummaries.joinToString("\n\n") { "«${it.chapterTitle}»:\n${it.compact}" }
        val fullStandard = chapterSummaries.joinToString("\n\n") { "📌 ${it.chapterTitle}:\n${it.standard}" }
        val fullDetailed = chapterSummaries.joinToString("\n\n") { "📖 ${it.chapterTitle}:\n${it.detailed}" }
        val fullNarrationReady = chapterSummaries.joinToString("\n\n---\n\n") { it.narrationReady }

        return BookSummaryResult(
            bookTitle = bookTitle,
            chapters = chapterSummaries,
            fullCompact = fullCompact,
            fullStandard = fullStandard,
            fullDetailed = fullDetailed,
            fullNarrationReady = fullNarrationReady
        )
    }

    private fun splitIntoChapters(text: String): List<String> {
        val lines = text.lines()
        val chapters = mutableListOf<String>()
        var current = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("فصل") || trimmed.startsWith("بخش") || trimmed.startsWith("Chapter", ignoreCase = true)) {
                if (current.isNotBlank()) {
                    chapters.add(current.toString().trim())
                    current = StringBuilder()
                }
            }
            current.append(line).append("\n")
        }
        if (current.isNotBlank()) {
            chapters.add(current.toString().trim())
        }

        return if (chapters.isEmpty()) listOf(text.trim()) else chapters
    }

    private fun extractChapterTitle(chapterText: String, defaultNum: Int): String {
        val firstLine = chapterText.lines().firstOrNull { it.isNotBlank() }?.trim() ?: "فصل $defaultNum"
        return if (firstLine.length < 50) firstLine else "فصل $defaultNum"
    }

    private fun generateCompactSummary(sentences: List<String>, title: String): String {
        if (sentences.isEmpty()) return title
        // Extract 2 key essence sentences
        val keySentences = sentences.filter { s ->
            s.contains("یعنی") || s.contains("زیرا") || s.contains("مهم") || s.contains("نتیجه") || s.contains("اصل") || s.length > 30
        }.take(2).ifEmpty { sentences.take(2) }

        return keySentences.joinToString(". ") + "."
    }

    private fun generateStandardSummary(sentences: List<String>, title: String): String {
        if (sentences.isEmpty()) return title
        // Take ~one-third of sentences preserving order
        val count = maxOf(2, (sentences.size / 3))
        val step = maxOf(1, sentences.size / count)
        val selected = sentences.filterIndexed { index, _ -> index % step == 0 }.take(count)
        return selected.joinToString(". ") + "."
    }

    private fun generateDetailedSummary(sentences: List<String>, title: String): String {
        if (sentences.isEmpty()) return title
        // Detailed preserves ~70% of logical statements
        val count = maxOf(3, (sentences.size * 2) / 3)
        return sentences.take(count).joinToString(". ") + "."
    }

    private fun formatForNarration(text: String, chapterTitle: String): String {
        val cleaned = text.replace(Regex("\\s+"), " ").trim()
        val sentences = cleaned.split(Regex("[.!?؟]+")).map { it.trim() }.filter { it.isNotBlank() }

        val sb = StringBuilder()
        sb.append("به نام آفریننده زیبایی‌ها...\n")
        sb.append("شنوندگان گرامی استودیوی آفرینا، توجه شما را به گویندگی «").append(chapterTitle).append("» جلب می‌کنیم.\n\n")

        for (s in sentences) {
            sb.append(s).append("... ")
        }
        sb.append("\n\nپایان این بخش از کتاب.")
        return sb.toString()
    }
}
