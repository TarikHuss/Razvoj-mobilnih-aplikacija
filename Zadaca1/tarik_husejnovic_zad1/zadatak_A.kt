interface Person {
    fun getAge(): Int
    fun getCountry(): String
}

open class Developer(
    private val firstName: String,
    private val lastName: String,
    private val age: Int,
    private val country: String,
    private val programmingLanguages: List<String>
) : Person {
    override fun getAge(): Int {return age} 
    override fun getCountry(): String {return country}
    fun getFullName(): String {return "$firstName $lastName" }
    fun getProgrammingLanguages(): List<String> {return programmingLanguages}
}

class BackendDeveloper(
    firstName: String,
    lastName: String,
    age: Int,
    country: String,
    programmingLanguages: List<String>,
    private val backendFramework: String
) : Developer(firstName, lastName, age, country, programmingLanguages) {
    fun getBackendFramework(): String {return backendFramework}
}

class FrontendDeveloper(
    firstName: String,
    lastName: String,
    age: Int,
    country: String,
    programmingLanguages: List<String>,
    private val frontendFramework: String
) : Developer(firstName, lastName, age, country, programmingLanguages) {
    fun getFrontendFramework(): String {return frontendFramework}
}

fun groupingByLanguages(developers: List<Developer>): Map<String, Int> {
    val listLanguages = developers.map{it.getProgrammingLanguages()}
     return listLanguages.flatten().groupingBy{it}.eachCount()
}

fun groupingByAverageAge(developers: List<Developer>): Map<String, Double> {
    val averageAgeByLanguage = developers.map { it.getProgrammingLanguages().map { language -> language to it.getAge()}}
    return averageAgeByLanguage.flatten().groupingBy {it.first}
    .fold(0 to 0) { sum, pair -> sum.first + pair.second to sum.second + 1 }
    .map{ (language, sumAgeByLanguage) -> language to sumAgeByLanguage.first.toDouble() / sumAgeByLanguage.second }
    .toMap()
}

/*  
Prednosti groupingBy: Kompaktniji je i čitljiviji kod, jer groupingBy omogućava jednostavnost u jednoj liniji koda.
Mane sa groupingBy: groupingBy može biti manje efikasan kod nekih složenijih izraza ili funkcija.
*/

fun countLanguagesWithoutGrouping(developers: List<Developer>): Map<String, Int> {
    val languageCount = mutableMapOf<String, Int>()
    for (developer in developers) {
        for (language in developer.getProgrammingLanguages()) {
            languageCount[language] = languageCount.getOrDefault(language, 0) + 1
        }
    }
    return languageCount
}

fun averageAgeWithoutGrouping(developers: List<Developer>): Map<String, Double> {
    val ageSum = mutableMapOf<String, Int>()
    val languageCount = mutableMapOf<String, Int>()

    for (developer in developers) {
        for (language in developer.getProgrammingLanguages()) {
            ageSum[language] = ageSum.getOrDefault(language, 0) + developer.getAge()
            languageCount[language] = languageCount.getOrDefault(language, 0) + 1
        }
    }
    return ageSum.toList().map{ (language, sum) -> language to sum.toDouble() / languageCount.getValue(language) }.toMap()
}

/*
Prednosti pristupa bez groupingBy: Više kontrole nad načinom brojanja, te može biti brži u određenim slučajevima.
Mane pristupa bez groupingBy: Kod je duži i može biti manje pregledan, što otežava održavanje i razumijvanje koda.
*/

fun printDeveloperData(developers: List<Developer>) {
    developers.forEach { developer -> val position = when (developer) {
            is BackendDeveloper -> "backend"
            is FrontendDeveloper -> "frontend"
            else -> "developer"
        }
        val framework = when (developer) {
            is BackendDeveloper -> developer.getBackendFramework()
            is FrontendDeveloper -> developer.getFrontendFramework()
            else -> "no framework"
        }
        println("${developer.getFullName()} je $position programer koji koristi ${developer.getProgrammingLanguages().joinToString(", ")} i $framework.")
    }
}
       
fun main() {
val developers = listOf(
    BackendDeveloper("Amila", "Residovic", 23, "BiH", listOf("Kotlin"), "Spring Boot"),
    BackendDeveloper("Ibrahim", "Selimovic", 22, "BiH", listOf("Java"), "Spring"),
    FrontendDeveloper("Emina", "Jusufovic", 23, "BiH", listOf("Kotlin"), "React"),
    FrontendDeveloper("Mujo", "Alic", 21, "BiH", listOf("JavaScript"), "Vue.js"),
    BackendDeveloper("Edin", "Drapic", 25, "BiH", listOf("Kotlin"), "Ktor")
)

printDeveloperData(developers)

}
