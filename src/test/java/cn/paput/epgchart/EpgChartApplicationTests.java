package cn.paput.epgchart;

import cn.paput.epgchart.search.JestService;
import cn.paput.epgchart.search.LowLevelRestClient;
import cn.paput.epgchart.search.RestService;
import cn.paput.epgchart.search.Program;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=EpgChartApplication.class)
public class EpgChartApplicationTests {

    @Autowired
    private RestService restService;
    @Autowired
    private JestService jestService;
    @Autowired
    private LowLevelRestClient lowLevelRestClient;

    public final static String isLike = "0";
    public final static String keyword = "b";
    public final static String folderIds = "2233,1581,966,545,4496,2547,941,1758,2436,46,945,3510,2864,3267,4848,1085,3669,3898,4983";
//    public final static String folderIds = null;
    public final static String spCode = null;
    public final static int from = 0;
    public final static int size = 8;

    @Test
    public void testSpeed() {
//        System.out.println(jestService.makeSQL(isLike, keyword, folderIds, spCode, from, size).toString());
//        System.out.println(restService.makeSQL(isLike, keyword, folderIds, spCode, from, size));
//        System.out.println(lowLevelRestClient.makeSQL(isLike, keyword, folderIds, spCode, from, size));
        StopWatch sw = new StopWatch("testSpeed");
        sw.start("queryByJest");
        queryByJest();
        sw.stop();
        sw.start("queryByRest");
        queryByRest();
        sw.stop();
        sw.start("queryByLowLevelRest");
        queryByLowLevelRest();
        sw.stop();
        System.out.println(sw.prettyPrint());

    }

    public void queryByJest() {
        List<Program> list = jestService.searchProgram(isLike, keyword, folderIds, spCode, from, size);
        System.out.println("queryByJest=" + list.toString());
    }

    public void queryByRest() {
        List<Program> list = restService.searchProgram(isLike, keyword, folderIds, spCode, from, size);
        System.out.println("queryByRest=" + list.toString());
    }

    public void queryByLowLevelRest() {
        List<Program> list = lowLevelRestClient.searchProgram(isLike, keyword, folderIds, spCode, from, size);
        System.out.println("queryByLowLevelRest=" + list.toString());
    }

}
