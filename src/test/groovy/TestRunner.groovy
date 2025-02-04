import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPResponse
import net.grinder.script.GTest
import net.grinder.script.Test
import org.junit.Before
import org.junit.Test
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import org.junit.runner.RunWith

@RunWith(GrinderRunner.class)
class TestRunner {

    private static final String BASE_URL = "http://localhost:8080/search"
    private static final HTTPRequest request = new HTTPRequest()

    @Before
    void beforeTest() {
        grinder.logger.info("Starting performance test...")
    }

    @Test
    void testPopularKeywords() {
        def test = new GTest(1, "Basic Aggregation")
        test.record(request)

        HTTPResponse response = request.GET(BASE_URL + "/popular")
        assert response.statusCode == 200
        grinder.logger.info("Response Time: ${response.time} ms")
    }

    @Test
    void testPopularKeywordsOptimized() {
        def test = new GTest(2, "Optimized Aggregation")
        test.record(request)

        HTTPResponse response = request.GET(BASE_URL + "/popular/optimized")
        assert response.statusCode == 200
        grinder.logger.info("Response Time: ${response.time} ms")
    }

    @Test
    void testPopularKeywordsFastest() {
        def test = new GTest(3, "Fastest Aggregation")
        test.record(request)

        HTTPResponse response = request.GET(BASE_URL + "/popular/fastest")
        assert response.statusCode == 200
        grinder.logger.info("Response Time: ${response.time} ms")
    }
}
