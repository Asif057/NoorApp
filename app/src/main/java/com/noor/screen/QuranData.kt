package com.noor.screen

import kotlin.random.Random

data class Ayat(
    val ayatNumber: Int,
    val surahName: String,
    val juzNumber: Int,
    val arabicText: String,
    val translation: String = ""
)

object QuranData {
    val ayatsList: List<Ayat> = listOf(
        // Surah Al-Fatihah (1-7) - Juz 1
        Ayat(
            ayatNumber = 1,
            surahName = "Surah Al-Fatihah",
            juzNumber = 1,
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            translation = "In the name of Allah, the Entirely Merciful, the Especially Merciful."
        ),
        Ayat(
            ayatNumber = 2,
            surahName = "Surah Al-Fatihah",
            juzNumber = 1,
            arabicText = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
            translation = "[All] praise is [due] to Allah, Lord of the worlds."
        ),
        Ayat(
            ayatNumber = 3,
            surahName = "Surah Al-Fatihah",
            juzNumber = 1,
            arabicText = "الرَّحْمَٰنِ الرَّحِيمِ",
            translation = "The Entirely Merciful, the Especially Merciful."
        ),
        Ayat(
            ayatNumber = 4,
            surahName = "Surah Al-Fatihah",
            juzNumber = 1,
            arabicText = "مَالِكِ يَوْمِ الدِّينِ",
            translation = "Sovereign of the Day of Recompense."
        ),
        Ayat(
            ayatNumber = 5,
            surahName = "Surah Al-Fatihah",
            juzNumber = 1,
            arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            translation = "It is You we worship and You we ask for help."
        ),
        Ayat(
            ayatNumber = 6,
            surahName = "Surah Al-Fatihah",
            juzNumber = 1,
            arabicText = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
            translation = "Guide us to the straight path."
        ),
        Ayat(
            ayatNumber = 7,
            surahName = "Surah Al-Fatihah",
            juzNumber = 1,
            arabicText = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
            translation = "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray."
        ),

        // Surah Al-Baqarah (1-8) - Juz 1
        Ayat(
            ayatNumber = 1,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "الم",
            translation = "Alif, Lam, Meem."
        ),
        Ayat(
            ayatNumber = 2,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ",
            translation = "This is the Book about which there is no doubt, a guidance for those conscious of Allah."
        ),
        Ayat(
            ayatNumber = 3,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ",
            translation = "Who believe in the unseen, establish prayer, and spend out of what We have provided for them."
        ),
        Ayat(
            ayatNumber = 4,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ",
            translation = "And who believe in what has been revealed to you and what was revealed before you, and of the Hereafter they are certain."
        ),
        Ayat(
            ayatNumber = 5,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ",
            translation = "Those are upon guidance from their Lord, and it is those who are the successful."
        ),
        Ayat(
            ayatNumber = 6,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "إِنَّ الَّذِينَ كَفَرُوا سَوَاءٌ عَلَيْهِمْ أَأَنذَرْتَهُمْ أَمْ لَمْ تُنذِرْهُمْ لَا يُؤْمِنُونَ",
            translation = "Indeed, those who disbelieve - it is all the same for them whether you warn them or do not warn them - they will not believe."
        ),
        Ayat(
            ayatNumber = 7,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "خَتَمَ اللَّهُ عَلَىٰ قُلُوبِهِمْ وَعَلَىٰ سَمْعِهِمْ ۖ وَعَلَىٰ أَبْصَارِهِمْ غِشَاوَةٌ ۖ وَلَهُمْ عَذَابٌ عَظِيمٌ",
            translation = "Allah has set a seal upon their hearts and upon their hearing, and over their vision is a veil. And for them is a great punishment."
        ),
        Ayat(
            ayatNumber = 8,
            surahName = "Surah Al-Baqarah",
            juzNumber = 1,
            arabicText = "وَمِنَ النَّاسِ مَن يَقُولُ آمَنَّا بِاللَّهِ وَبِالْيَوْمِ الْآخِرِ وَمَا هُم بِمُؤْمِنِينَ",
            translation = "And of the people are some who say, 'We believe in Allah and the Last Day,' but they are not believers."
        )
    )

    fun getSequentialAyats(count: Int = 5): List<Ayat> {
        val maxStartIndex = (ayatsList.size - count).coerceAtLeast(0)
        val startIndex = if (maxStartIndex > 0) Random.nextInt(0, maxStartIndex + 1) else 0
        return ayatsList.subList(startIndex, (startIndex + count).coerceAtMost(ayatsList.size))
    }
}
