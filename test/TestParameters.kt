import io.ktor.http.Parameters

class TestParameters(
    vararg initialParameters: Pair<String, String>
) : Parameters {
    override val caseInsensitiveName: Boolean = false

    private val params: MutableSet<Map.Entry<String, List<String>>> = mutableSetOf()

    override fun entries(): Set<Map.Entry<String, List<String>>> = params

    override fun getAll(name: String): List<String>? = params.find { it.key == name }?.value

    override fun isEmpty(): Boolean = params.isEmpty()

    override fun names(): Set<String> = params.map { it.key }.toSet()

    operator fun set(s: String, value: String) = params.add(Param(s, listOf(value)))

    override operator fun get(name: String): String? {
        val listOfParams = getAll(name)
            ?: return null

        if (listOfParams.size != 1)
            throw IllegalArgumentException("Too many params for custom params class")

        return listOfParams.component1()
    }

    init {
        for (parameter in initialParameters) {
            this[parameter.first] = parameter.second
        }
    }

    class Param(
        override val key: String,
        override val value: List<String>
    ) : Map.Entry<String, List<String>>
}